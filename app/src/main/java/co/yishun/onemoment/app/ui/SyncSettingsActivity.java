package co.yishun.onemoment.app.ui;

import android.view.View;
import android.widget.Switch;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.AccountHelper;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_sync_settings)
public class SyncSettingsActivity extends ToolbarBaseActivity {

    @ViewById
    Switch syncAutoSwitch;
    @ViewById
    Switch wifiSyncSwitch;

    @Click void syncAutoItemClicked(View view) {
        syncAutoSwitch.setChecked(!syncAutoSwitch.isChecked());
    }

    @Click void wifiSyncSwitchClicked(View view) {
        wifiSyncSwitch.setChecked(!wifiSyncSwitch.isChecked());
    }

    @Click void syncNowItemClicked(View view) {
        AccountHelper.syncAtOnce(this);
        showNotification(R.string.syncSettingsSyncNowToast);
    }

    @AfterViews void setSwitch() {
        boolean enable = AccountHelper.isAutoSync(this);
        syncAutoSwitch.setChecked(enable);
        syncAutoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AccountHelper.setAutoSync(this, isChecked);
            wifiSyncSwitch.setEnabled(isChecked);
        });

        wifiSyncSwitch.setChecked(AccountHelper.isWifiSyncEnable(this));
        wifiSyncSwitch.setEnabled(enable);
        wifiSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AccountHelper.setWifiSyncEnable(this, isChecked);
        });
    }
}
