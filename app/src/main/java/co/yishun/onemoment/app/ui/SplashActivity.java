package co.yishun.onemoment.app.ui;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.ImageView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.notification.EveryDayNotification;
import co.yishun.onemoment.app.ui.guide.GuideActivity_;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2015/4/3.
 */
@Fullscreen
@EActivity(R.layout.activity_splash)
public class SplashActivity extends BaseActivity {

    @ViewById
    ImageView splashImageView;

    private boolean back = false;

    @Override
    protected void onResume() {
        super.onResume();
        startRecording();
    }

    @AfterViews
    void setNotification() {
        EveryDayNotification.checkNotification(this);
    }

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
        return getSharedPreferences(Config.PREFERENCE, MODE_PRIVATE).getBoolean(Config.PREFERENCE_IS_FIRST_LAUNCH, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back = true;
    }

    @UiThread(delay = 1600)
    void startRecording() {
        this.finish();
        if (!back) {
            overridePendingTransition(R.anim.act_fade_in, R.anim.act_fade_out);
            if (isFirstLaunch()) GuideActivity_.intent(this).start();
            else RecordingActivity_.intent(this).start();
        }
    }

}
