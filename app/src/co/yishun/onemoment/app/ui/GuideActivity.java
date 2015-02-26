package co.yishun.onemoment.app.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import co.yishun.onemoment.app.MainActivity_;
import co.yishun.onemoment.app.R;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

/**
 * This activity is to guide user when first launched.
 * <p/>
 * Created by Carlos on 2/7/15.
 */
@EActivity(R.layout.guide_activity)
public class GuideActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Click(R.id.ok)
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                onGuideFinish();
                break;
        }
    }

    @UiThread
    void onGuideFinish() {
        changeEnableStatus();
    }

    /**
     * After user finished this guide, disable this activity, and enable {@link co.yishun.onemoment.app.ui.RecordActivity_} as default activity.
     */
    private void changeEnableStatus() {
        PackageManager packageManager = getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(this, RecordingActivity_.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        packageManager.setComponentEnabledSetting(new ComponentName(this, GuideActivity_.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}