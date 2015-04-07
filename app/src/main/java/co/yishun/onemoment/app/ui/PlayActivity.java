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
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2015/4/5.
 */
@EActivity(R.layout.activity_play)
public class PlayActivity extends ToolbarBaseActivity {
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
    Moment moment;

    @AfterViews
    void initVideo() {
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

        Picasso.with(this).load("file://" + moment.getLargeThumbPath()).into(thumbImageView);
        videoView.setVideoPath(moment.getPath());
        videoView.setOnCompletionListener(mp -> {
            mp.reset();
            videoView.setVideoPath(moment.getPath());

            thumbImageView.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.VISIBLE);
        });
    }

//    @Click
//    void thumbImageViewClicked(View view) {
//        view.setVisibility(View.GONE);
//        videoView.start();
//    }

    @Click
    void playBtnClicked(View view) {
        view.setVisibility(View.GONE);
        thumbImageView.setVisibility(View.GONE);
        videoView.start();
    }
}
