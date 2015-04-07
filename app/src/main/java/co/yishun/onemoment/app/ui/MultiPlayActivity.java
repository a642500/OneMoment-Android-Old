package co.yishun.onemoment.app.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;
import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.Moment;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Created by Carlos on 2015/4/5.
 */
@EActivity(R.layout.activity_multi_play)
public class MultiPlayActivity extends ToolbarBaseActivity {
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

    @AfterViews
    void setSquare() {
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

    @AfterViews
    void initVideo() {
        toPlayMoments = new ArrayDeque<>(moments.size());
        queueMoments();
        if (BuildConfig.DEBUG && !(moments.size() >= 2))
            throw new RuntimeException("moment's length is 1 or 0, use PlayActivity");

        Picasso.with(this).load("file://" + moments.get(0).getLargeThumbPath()).into(thumbImageView);
        //TODO add cover video

        videoView.setOnCompletionListener(mp -> {
            mp.reset();
            Moment moment = toPlayMoments.poll();
            if (moment != null) {
                videoView.setVideoPath(moment.getPath());
                videoView.start();
            } else {
                playBtn.setVisibility(View.VISIBLE);
                thumbImageView.setVisibility(View.VISIBLE);
                queueMoments();
            }
        });
    }

    @Click
    void playBtnClicked(View view) {
        videoView.setVideoPath(toPlayMoments.poll().getPath());
        view.setVisibility(View.GONE);
        videoView.start();
        thumbImageView.setVisibility(View.GONE);
    }

    @Click
    void shareVideoBtnClicked(View view) {
        //TODO
    }
}
