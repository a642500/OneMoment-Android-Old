package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import co.yishun.onemoment.app.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;


@EActivity(R.layout.activity_video_save)
public class VideoSaveActivity extends ActionBarActivity {
    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(getString(R.string.videoSaveTitle));
        toolbar.setNavigationOnClickListener(v -> VideoSaveActivity.this.onBackPressed());
    }



}
