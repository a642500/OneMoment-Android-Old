package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_identity_info)
public class IdentityInfoActivity extends ActionBarActivity {
    private static final String TAG = LogUtil.makeTag(IdentityInfoActivity.class);

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(getString(R.string.identityInfoTitle));
        toolbar.setNavigationOnClickListener(v -> IdentityInfoActivity.this.onBackPressed());
    }


}
