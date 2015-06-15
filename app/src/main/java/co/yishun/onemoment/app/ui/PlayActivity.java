package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.net.request.sync.GetToken;
import co.yishun.onemoment.app.sync.SyncAdapter;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.squareup.picasso.Picasso;
import me.toxz.squarethumbnailvideoview.library.SingleVideoAdapter;
import me.toxz.squarethumbnailvideoview.library.SquareThumbnailVideoView;
import org.androidannotations.annotations.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Carlos on 2015/4/5.
 */
@EActivity(R.layout.activity_play)
public class PlayActivity extends ToolbarBaseActivity implements SyncAdapter.OnCheckedListener {
    private static final String TAG = LogUtil.makeTag(PlayActivity.class);
    @ViewById
    ViewGroup shareVideoBtnParent;
    @ViewById
    SquareThumbnailVideoView squareThumbnailVideoView;
    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class) Dao<Moment, Integer> dao;
    @Extra boolean isReplayAll;
//    @Extra
//    String videoPath;
//    @Extra
//    String largeThumbPath;

    @Extra
    Moment moment;
    boolean isLock = false;

//    @AfterViews void initVideoView() {
//        ViewTreeObserver viewTreeObserver = recordSurfaceParent.getViewTreeObserver();
//        if (viewTreeObserver.isAlive()) {
//            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    recordSurfaceParent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                    ViewGroup.LayoutParams params = recordSurfaceParent.getLayoutParams();
//                    params.height = recordSurfaceParent.getHeight();
//                    params.width = recordSurfaceParent.getWidth();
//                }
//            });
//        }
//    }

    @Override protected void onResume() {
        super.onResume();
        checkMoment();
    }

    @AfterViews void setShareVideoBtn() {
        LogUtil.i(TAG, "setShareVideoBtn: " + isReplayAll);
        if (isReplayAll) {
            shareVideoBtnParent.setVisibility(View.VISIBLE);
        }
    }

    @Click void shareVideoBtnClicked(View view) {
        if (!AccountHelper.isLogin(this)) {
            showNotification(R.string.multiPlayShareLoginAlert);
        } else
            prepareShare();
    }

    @Background void prepareShare() {
        showProgress();

        if (moment != null) {
            String key = getQiniuVideoFileName(moment);
            upload(moment.getPath(), key);
        } else {
            shareFail();
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
                                share(qiNiuKey);
                            else shareFail();
                        },
                        new UploadOptions(null, Config.MIME_TYPE, true, null, null)
                );
        });
    }

    private void share(String key) {
        hideProgress();
        String url = Config.URL_SHARE_LONG_VIDEO + key;
        String msg = getString(R.string.multiPlayShareText);

        Intent sendIntent = new Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, msg + url).setType("text/plain");
        postStartActivity(sendIntent);
    }

    private void shareFail() {
        hideProgress();
        showNotification(R.string.multiPlayShareFail);
    }

    @UiThread(delay = 200) void postStartActivity(Intent intent) {
        startActivity(intent);
    }

    private String getQiniuVideoFileName(Moment moment) {
        return Config.LONG_VIDEO_PREFIX + AccountHelper.getIdentityInfo(this).get_id() + Config.URL_HYPHEN + 1 + Config.URL_HYPHEN + moment.getTimeStamp() + Config.VIDEO_FILE_SUFFIX;
    }

    /**
     * lock moment write lock and refresh moment data
     */
    @Background void checkMoment() {
        Moment.lock(this);
        try {
            LogUtil.i(TAG, "check moment origin: " + moment);
            Where<Moment, Integer> w = dao.queryBuilder().orderBy("time", true).where();
            List<Moment> moments;
            if (AccountHelper.isLogin(this)) {
                moments = w.and(
                        w.eq("time", moment.getTime()),
                        w.eq("owner", "LOC").or().eq("owner", AccountHelper.getIdentityInfo(this).get_id())//null when not login
                ).query();
            } else {
                moments = w.eq("time", moment.getTime()).and().eq("owner", "LOC").query();
            }
            if (moments.size() > 0) moment = moments.get(0);
            LogUtil.i(TAG, "check moment after: " + moment);
            SyncAdapter.checkAndSolveBadMoment(moment, this, this);
        } catch (SQLException e) {
            this.finish();
            LogUtil.e(TAG, "SQL exception");
            e.printStackTrace();
        }
    }

    //    @Click
//    void thumbImageViewClicked(View view) {
//        view.setVisibility(View.GONE);
//        videoView.start();
//    }

    @Override protected void onPause() {
        super.onPause();
//        videoView.stopPlayback();
        Moment.unlock();
    }

    @Override @UiThread public void onMomentOk(Moment moment) {
        hideProgress();
        isLock = true;
        squareThumbnailVideoView.setVideoAdapter(new SingleVideoAdapter() {
            @Override public String getVideoPath(int position) {
                return moment.getPath();
            }

            @Override public boolean setThumbnailImage(ImageView thumbnailImageView, Bitmap bitmap) {
                Picasso.with(PlayActivity.this).load("file://" + moment.getLargeThumbPath()).into(thumbnailImageView);
                return true;
            }
        });
    }

    @Override @UiThread public void onMomentStartRepairing(Moment moment) {
        showProgress(R.string.playRepairing);
    }

    @Override @UiThread public void onMomentDelete(Moment moment) {
        showNotification(R.string.playRepairingFailed);
        delayFinish();
    }

    @UiThread(delay = 2000) void delayFinish() { finish(); }
}
