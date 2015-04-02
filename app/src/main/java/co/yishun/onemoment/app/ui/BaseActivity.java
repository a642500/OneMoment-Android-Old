package co.yishun.onemoment.app.ui;

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
public abstract class BaseActivity extends ActionBarActivity {

    @ViewById
    Toolbar toolbar;

    @UiThread
    void showNotification(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void showNotification(int textRes) {
        showNotification(getString(textRes));
    }

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
//        toolbar.setTitle(getString(R.string.integrateInfoTitle));
        toolbar.setNavigationOnClickListener(v -> BaseActivity.this.onBackPressed());
    }

    private MaterialDialog mProgressDialog;

    @UiThread
    void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(this).theme(Theme.DARK).progress(true, 0).content(R.string.signUpLoading).build();
        }
        mProgressDialog.show();
    }

    @UiThread
    void hideProgress() {
        mProgressDialog.hide();
    }

}
