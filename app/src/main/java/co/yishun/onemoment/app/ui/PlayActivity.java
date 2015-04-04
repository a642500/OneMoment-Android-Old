package co.yishun.onemoment.app.ui;

import android.view.View;
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

//    @Extra
//    String videoPath;
//    @Extra
//    String largeThumbPath;

    @Extra
    Moment moment;

    @AfterViews
    void initVideo() {
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
