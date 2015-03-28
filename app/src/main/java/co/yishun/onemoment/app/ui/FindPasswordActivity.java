package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import co.yishun.onemoment.app.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_find_password)
public class FindPasswordActivity extends ActionBarActivity {
    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(getString(R.string.findPasswordTitle));
        toolbar.setNavigationOnClickListener(v -> FindPasswordActivity.this.onBackPressed());
    }
}
