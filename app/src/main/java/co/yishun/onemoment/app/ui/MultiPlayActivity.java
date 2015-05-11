package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.convert.Converter;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.net.request.sync.GetToken;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Created by Carlos on 2015/4/5.
 */
@EActivity(R.layout.activity_multi_play)
public class MultiPlayActivity extends ToolbarBaseActivity implements MediaPlayer.OnPreparedListener {
    private static final String TAG = LogUtil.makeTag(MultiPlayActivity.class);
    @ViewById
    VideoView videoView;
    @ViewById
    ImageView thumbImageView;
    @ViewById
    ImageButton playBtn;

    @ViewById
    FrameLayout recordSurfaceParent;

//    @Extra
//    String videoPath;
//    @Extra
//    String largeThumbPath;

    @Extra
    List<Moment> moments;

    Queue<Moment> toPlayMoments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @AfterViews void setSquare() {
        ViewTreeObserver viewTreeObserver = recordSurfaceParent.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    recordSurfaceParent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    ViewGroup.LayoutParams params = recordSurfaceParent.getLayoutParams();
                    params.height = recordSurfaceParent.getHeight();
                    params.width = recordSurfaceParent.getWidth();
                }
            });
        }
    }

    private void queueMoments() {
        toPlayMoments.clear();
        toPlayMoments.addAll(moments);
    }

    @AfterViews void initVideo() {
        LogUtil.i(TAG, "moments list, " + Arrays.toString(moments.toArray()));
        toPlayMoments = new ArrayDeque<>(moments.size());
        queueMoments();
        if (BuildConfig.DEBUG && !(moments.size() >= 2))
            throw new RuntimeException("moment's length is 1 or 0, use PlayActivity");

        Picasso.with(this).load("file://" + moments.get(0).getLargeThumbPath()).into(thumbImageView);
        //TODO add cover video


        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(mp -> {
            mp.reset();
            Moment moment = toPlayMoments.poll();
            moment = ensureMomentUpdated(moment);
            if (moment != null) {
                videoView.setVideoPath(moment.getPath());
                videoView.start();
            } else {
                Moment.unlock();
                videoView.setOnPreparedListener(MultiPlayActivity.this);
                MultiPlayActivity.this.queueMoments();
                playBtn.setVisibility(View.VISIBLE);
                thumbImageView.setVisibility(View.VISIBLE);
            }
        });
    }


    @Click void playBtnClicked(View view) {
        Moment.lock(MultiPlayActivity.this);

        Moment toPlay = toPlayMoments.poll();
        toPlay = ensureMomentUpdated(toPlay);
        if (toPlay == null) {
            this.finish();
            Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            return;
        }
        videoView.setVideoPath(toPlay.getPath());
        view.setVisibility(View.GONE);
    }

    @OrmLiteDao(helper = MomentDatabaseHelper.class)
    Dao<Moment, Integer> dao;

    /**
     * To update moment is latest. You should lock Moment before call this method.
     *
     * @param moment may be invalid
     * @return null or valid moment.
     */
    private Moment ensureMomentUpdated(Moment moment) {
        if (moment == null) return null;
        Where<Moment, Integer> w = dao.queryBuilder().orderBy("time", true).where();
        List<Moment> momentQueried = null;
        try {
            if (AccountHelper.isLogin(this)) {
                momentQueried = w.and(
                        w.eq("time", moment.getTime()),
                        w.eq("owner", "LOC").or().eq("owner", AccountHelper.getIdentityInfo(this).get_id())//null when not login
                ).query();

            } else {
                momentQueried = w.eq("time", moment.getTime()).and().eq("owner", "LOC").query();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (momentQueried != null && momentQueried.size() > 0) return momentQueried.get(0);
        else return null;
    }

    @Click void shareVideoBtnClicked(View view) {
        if (!AccountHelper.isLogin(this)) {
            showNotification(R.string.multiPlayShareLoginAlert);
        } else
            prepareShare();
    }

    long timestamp = 0;

    @Background void prepareShare() {
        showProgress();
        int toMergeVideoCount = moments.size() > 10 ? 10 : moments.size();
        File dir = getDir("lone_video", MODE_PRIVATE);
        String key = getQiniuVideoFileName(toMergeVideoCount);
        File output = new File(dir.getPath() + File.pathSeparator + key);
        File listText = new File(dir.getPath() + File.pathSeparator + "list.txt");
        if (listText.exists()) listText.delete();
        try {
            listText.createNewFile();
            PrintWriter writer = new PrintWriter(listText);
            for (int i = 0; i < toMergeVideoCount; i++) {
                writer.println("file " + moments.get(i).getPath());
            }
            writer.close();

            Converter.with(this).merge(listText.getPath()).to(output.getPath()).setHandler(new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    LogUtil.i(TAG, "success: " + message);
                }

                @Override
                public void onProgress(String message) {
                    LogUtil.i(TAG, "progress: " + message);

                }

                @Override
                public void onFailure(String message) {
                    LogUtil.e(TAG, "fail: " + message);
                    shareFail();
                }

                @Override
                public void onStart() {
                    LogUtil.i(TAG, "start");
                    LogUtil.i(TAG, "Converter2 onStart time: " + System.currentTimeMillis());

                }

                @Override
                public void onFinish() {
                    LogUtil.i(TAG, "Converter2 onFinish time: " + System.currentTimeMillis());
                    LogUtil.i(TAG, "finish");
                    upload(output.getPath(), key);
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background void upload(String path, String qiNiuKey) {
        LogUtil.i(TAG, "upload a long video: " + qiNiuKey + ", path: " + path);
        new GetToken().setFileName(qiNiuKey).with(this).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
            } else if (result.getCode() != ErrorCode.SUCCESS) LogUtil.e(TAG, "get token failed: " + result.getCode());
            else
                new UploadManager().put(path,
                        qiNiuKey,
                        result.getData().getToken(),
                        (s, responseInfo, jsonObject) -> {
                            LogUtil.i(TAG, responseInfo.toString());
                            LogUtil.i(TAG, "a moment upload ok: " + path);
                            if (responseInfo.isOK())
                                share();
                            else shareFail();
                        },
                        new UploadOptions(null, Config.MIME_TYPE, true, null, null)
                );
        });
    }

    private void share() {
        hideProgress();
        String url = Config.URL_SHARE_LONG_VIDEO + AccountHelper.getIdentityInfo(this).get_id() + "&ts=" + timestamp;
        String msg = getString(R.string.multiPlayShareText);

        Intent sendIntent = new Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, msg + url).setType("text/plain");
        postStartActivity(sendIntent);
    }

    /**
     * delay to wait animation ending.
     */
    @UiThread(delay = 200) void postStartActivity(Intent intent) {
        startActivity(intent);
    }

    private void shareFail() {
        hideProgress();
        showNotification(R.string.multiPlayShareFail);
    }


    private String getQiniuVideoFileName(int count) {
        timestamp = System.currentTimeMillis();
        return Config.LONG_VIDEO_PREFIX + AccountHelper.getIdentityInfo(this).get_id() + Config.URL_HYPHEN + count + Config.URL_HYPHEN + timestamp + Config.VIDEO_FILE_SUFFIX;
    }

    @Override public void onPrepared(MediaPlayer mp) {
        if (Build.VERSION.SDK_INT >= 17) {
            videoView.setOnInfoListener((mp1, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    thumbImageView.setVisibility(View.GONE);
                }
                videoView.setOnInfoListener(null);
                return true;
            });
        } else {
            thumbImageView.setVisibility(View.GONE);
        }
        videoView.start();
        videoView.setOnPreparedListener(null);

    }
}
