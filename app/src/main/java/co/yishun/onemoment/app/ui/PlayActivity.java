package co.yishun.onemoment.app.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.sync.SyncAdapter;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;
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
    VideoView videoView;
    @ViewById
    ImageView thumbImageView;
    @ViewById
    ImageButton playBtn;
    @ViewById
    FrameLayout recordSurfaceParent;
    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class)
    Dao<Moment, Integer> dao;
//    @Extra
//    String videoPath;
//    @Extra
//    String largeThumbPath;

    @Extra
    Moment moment;

    @AfterViews void initVideoView() {
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

    @Override protected void onResume() {
        super.onResume();
        checkMoment();
    }

    /**
     * lock moment write lock and refresh moment data
     */
    @Background void checkMoment() {
        Moment.lock(this);
        try {
            List<Moment> moments = dao.queryBuilder().orderBy("time", false).where().eq("time", moment.getTime()).query();
            if (moments.size() > 0) moment = moments.get(0);
            SyncAdapter.checkAndSolveBadMoment(moment, this, this);
        } catch (SQLException e) {
            this.finish();
            LogUtil.e(TAG, "SQL exception");
            e.printStackTrace();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        videoView.stopPlayback();
        Moment.unlock();
    }

    //    @Click
//    void thumbImageViewClicked(View view) {
//        view.setVisibility(View.GONE);
//        videoView.start();
//    }

    @Click void playBtnClicked(View view) {
        view.setVisibility(View.GONE);
        thumbImageView.setVisibility(View.GONE);
        videoView.start();
    }

    boolean isLock = false;

    @Override @UiThread public void onMomentOk(Moment moment) {
        hideProgress();
        isLock = true;

        Picasso.with(this).load("file://" + moment.getLargeThumbPath()).into(thumbImageView);
        videoView.setVideoPath(moment.getPath());
        videoView.setOnCompletionListener(mp -> {
            mp.reset();
            videoView.setVideoPath(moment.getPath());
            thumbImageView.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.VISIBLE);
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
