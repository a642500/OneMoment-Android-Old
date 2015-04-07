package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;
import co.yishun.onemoment.app.R;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;


@EActivity(R.layout.activity_video_save)
public class VideoSaveActivity extends ToolbarBaseActivity {
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
        ViewGroup.LayoutParams params = recordSurfaceParent.getLayoutParams();
        int min = Math.min(params.height, params.width);
        params.height = min;
        params.width = min;
        recordSurfaceParent.setLayoutParams(params);


        Picasso.with(this).load("file://" + largeThumbPath).into(thumbImageView);
        videoView.setVideoPath(videoPath);
        videoView.setOnCompletionListener(mp -> {
            videoView.setVideoPath(videoPath);
            videoView.setOnClickListener(v -> videoView.start());
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
        this.finish();
    }
}
