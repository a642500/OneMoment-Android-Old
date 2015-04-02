package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Carlos on 2015/4/2.
 */
@EActivity
public class ToolbarBaseActivity extends BaseActivity {

    @ViewById
    public Toolbar toolbar;

    @AfterViews
    public void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> ToolbarBaseActivity.this.onBackPressed());
    }
}
