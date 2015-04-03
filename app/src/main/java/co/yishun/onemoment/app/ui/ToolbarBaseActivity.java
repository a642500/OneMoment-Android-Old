package co.yishun.onemoment.app.ui;

import android.support.v7.widget.Toolbar;
import android.view.View;
import co.yishun.onemoment.app.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToolbarBaseActivity.this.onBackPressed();
            }
        });
    }
}
