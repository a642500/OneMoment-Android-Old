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
    Switch onlyWifiSyncSwitch;
    @ViewById
    View onlyWifiSyncItem;

    @Click void syncAutoItemClicked(View view) {
        syncAutoSwitch.setChecked(!syncAutoSwitch.isChecked());
    }

    @Click void onlyWifiSyncItemClicked(View view) {
        onlyWifiSyncSwitch.setChecked(!onlyWifiSyncSwitch.isChecked());
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
            onlyWifiSyncSwitch.setEnabled(isChecked);
            onlyWifiSyncItem.setEnabled(isChecked);
        });

        onlyWifiSyncSwitch.setChecked(AccountHelper.isOnlyWifiSyncEnable(this));
        onlyWifiSyncSwitch.setEnabled(enable);
        onlyWifiSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AccountHelper.setOnlyWifiSyncEnable(this, isChecked);
        });
    }
}
