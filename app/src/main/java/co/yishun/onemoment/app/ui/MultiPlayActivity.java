package co.yishun.onemoment.app.ui;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;
import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.Moment;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

import java.util.List;

/**
 * Created by Carlos on 2015/4/5.
 */
@EActivity(R.layout.activity_multi_play)
public class MultiPlayActivity extends ToolbarBaseActivity {
    @ViewById
    VideoView videoView;
    @ViewById
    VideoView videoViewAnother;
    @ViewById
    ImageView thumbImageView;
    @ViewById
    ImageButton playBtn;

//    @Extra
//    String videoPath;
//    @Extra
//    String largeThumbPath;

    @Extra
    List<Moment> moments;

    int point = 0;
    boolean pendingEnd = false;

    @AfterViews
    void initVideo() {
        if (BuildConfig.DEBUG && !(moments.size() >= 2))
            throw new RuntimeException("moment's length is 1 or 0, use PlayActivity");

        Picasso.with(this).load("file://" + moments.get(0).getLargeThumbPath()).into(thumbImageView);
        videoView.setVideoPath(moments.get(point).getPath());
        point++;

        videoView.setOnCompletionListener(mp -> {
            mp.reset();
            if (pendingEnd) {
                end();
                return;
            }

            videoViewAnother.start();
            videoViewAnother.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);
            if (point < moments.size()) {
                videoView.setVideoPath(moments.get(point).getPath());
                point++;
            } else pendingEnd = true;
        });
        videoViewAnother.setOnCompletionListener(mp -> {
            mp.reset();
            if (pendingEnd) {
                end();
                return;
            }

            videoView.start();
            videoViewAnother.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            if (point < moments.size()) {
                videoViewAnother.setVideoPath(moments.get(point).getPath());
                point++;
            } else pendingEnd = true;
        });
    }

    private void end() {
        thumbImageView.setVisibility(View.VISIBLE);
        playBtn.setVisibility(View.VISIBLE);
        point = 0;
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

        videoViewAnother.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
        videoViewAnother.setVideoPath(moments.get(point).getPath());
        point++;
    }

    @Click
    void shareVideoBtnClicked(View view) {
        //TODO
    }
}
