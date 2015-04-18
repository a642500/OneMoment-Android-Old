package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.LogUtil;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;


@EActivity(R.layout.activity_video_save)
public class VideoSaveActivity extends ToolbarBaseActivity {
    private static final String TAG = LogUtil.makeTag(VideoSaveActivity.class);

    @ViewById
    VideoView videoView;
    @ViewById
    ImageView thumbImageView;

    @ViewById
    FrameLayout recordSurfaceParent;

    @Extra
    String videoPath;
    @Extra
    String largeThumbPath;

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

        Picasso.with(this).load("file://" + largeThumbPath).into(thumbImageView);
        videoView.setVideoPath(videoPath);
        videoView.setOnCompletionListener(mp -> {
            mp.reset();
            videoView.setVideoPath(videoPath);
            thumbImageView.setVisibility(View.VISIBLE);
        });
    }

    @Click
    void thumbImageViewClicked(View view) {
        view.setVisibility(View.GONE);
        videoView.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED, getIntent());
    }

    @Click
    void saveVideoBtnClicked(View view) {
        setResult(RESULT_OK, getIntent());
        postFinish();
    }

    /**
     * delay to wait animation ending.
     */
    @UiThread(delay = 200)
    void postFinish() {
        this.finish();
    }
}
