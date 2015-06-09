package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


@EActivity(R.layout.activity_video_save)
public class VideoSaveActivity extends ToolbarBaseActivity {
    private static final String TAG = LogUtil.makeTag(VideoSaveActivity.class);

    @ViewById VideoView videoView;
    @ViewById ImageView thumbImageView;
    @ViewById FrameLayout recordSurfaceParent;

    @Extra String videoPath;
    @Extra String thumbPath;
    @Extra String largeThumbPath;
    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class) Dao<Moment, Integer> momentDao;
    /**
     * sign whether video need to save
     */
    private boolean pendingSave = false;// set false default to delete it if user press back directly.

    @AfterViews void initVideo() {
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

        Picasso.with(this).load("file://" + largeThumbPath).into(thumbImageView);
        videoView.setVideoPath(videoPath);
        videoView.setOnCompletionListener(mp -> {
            mp.reset();
            videoView.setVideoPath(videoPath);
            thumbImageView.setVisibility(View.VISIBLE);
        });
    }

    @Click void thumbImageViewClicked(View view) {
        view.setVisibility(View.GONE);
        videoView.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED, getIntent());
    }

    @Click void saveVideoBtnClicked(View view) {
//        setResult(RESULT_OK, getIntent());
        pendingSave = true;
        postFinish();
    }

    /**
     * To solve pending saving task
     */
    private void handlePendingSaveVideo() {
        Moment.lock(this);
        if (pendingSave) {
            pendingSave = false;
            //register at database
            LogUtil.i(TAG, "start saving");
            try {
                // query other today's moment pending deleting
                String time = new SimpleDateFormat(Config.TIME_FORMAT).format(Calendar.getInstance().getTime());
                List<Moment> result;
                Where<Moment, Integer> w = momentDao.queryBuilder().where();
                if (AccountHelper.isLogin(this)) {
                    result = w.and(
                            w.eq("time", time),
                            w.or(w.eq("owner", AccountHelper.getIdentityInfo(this).get_id()),
                                    w.eq("owner", "LOC"))).query();
                } else result = w.eq("time", time).and().eq("owner", "LOC").query();

                LogUtil.i(TAG, "old today moment to delete: " + Arrays.toString(result.toArray()));

                // create new moment
                Moment moment = new Moment.MomentBuilder()
                        .setPath(videoPath)
                        .setThumbPath(thumbPath)
                        .setLargeThumbPath(largeThumbPath)
                        .build();

                // delete old
                if (1 == momentDao.create(moment)) {
                    LogUtil.i(TAG, "new moment: " + moment);
                    momentDao.delete(result);
                    for (Moment mToDe : result) {
                        new File(mToDe.getLargeThumbPath()).delete();
                        new File(mToDe.getThumbPath()).delete();
                        new File(mToDe.getPath()).delete();
                    }
                }

                // go to album act
                AlbumActivity_.intent(this).start();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "not pending saving, delete useless video and thumbnails file");
            File useless = new File(largeThumbPath);
            if (useless.exists()) useless.delete();
            useless = new File(videoPath);
            if (useless.exists()) useless.delete();
            useless = new File(thumbPath);
            if (useless.exists()) useless.delete();
        }
        Moment.unlock();
    }

    /**
     * Delay to wait animation ending.
     */
    @UiThread(delay = 300) void postFinish() {
        this.finish();
    }

    @Override public void onBackPressed() {
        handlePendingSaveVideo();
        super.onBackPressed();
    }

    @Override public void finish() {
        handlePendingSaveVideo();
        super.finish();
    }
}
