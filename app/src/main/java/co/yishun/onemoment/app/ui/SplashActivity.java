package co.yishun.onemoment.app.ui;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.ImageView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.ui.guide.GuideActivity_;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2015/4/3.
 */
@Fullscreen
@EActivity(R.layout.activity_splash)
public class SplashActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        startRecording();
    }

    @ViewById
    ImageView splashImageView;

    @AfterViews
    void setResource() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        if (hasBackKey && hasHomeKey) {
            // no navigation bar, unless it is enabled in the settings
            splashImageView.setImageResource(R.drawable.bg_welcome_no_nav);
        } else {
            // 99% sure there's a navigation bar
            splashImageView.setImageResource(R.drawable.bg_welcome);
        }
    }

    boolean isFirstLaunch() {
        return getSharedPreferences(Config.PREFERENCE, MODE_PRIVATE).getBoolean("is_first_launch", true);
    }

    @UiThread(delay = 2000)
    void startRecording() {
        this.finish();
        if (isFirstLaunch()) GuideActivity_.intent(this).start();
        else RecordingActivity_.intent(this).start();
    }

}
