package co.yishun.onemoment.app.ui;

import android.support.v7.app.AppCompatActivity;
import co.yishun.onemoment.app.R;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.nispok.snackbar.Snackbar;
import com.umeng.analytics.MobclickAgent;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Carlos on 2015/4/2.
 */
@EActivity
public class BaseActivity extends AppCompatActivity {

    private MaterialDialog mProgressDialog;

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @UiThread
    public void showNotification(String text) {
        Snackbar.with(getApplicationContext()).text(text).show(this);
    }

    @UiThread
    public void showNotification(int textRes) {
        showNotification(getString(textRes));
    }

    public void showProgress() {
        showProgress(R.string.signUpLoading);
    }

    @UiThread
    public void showProgress(String text) {
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(this).theme(Theme.DARK).progress(true, 0).content(text).build();
        }
        mProgressDialog.show();
    }

    public void showProgress(int stringRes) {
        showProgress(getResources().getString(stringRes));
    }

    @UiThread
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//    }
//
//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//    }
//
//    @Override
//    public void startActivity(Intent intent) {
//        super.startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
//    }
//
//    @Override
//    public void startActivityForResult(Intent intent, int requestCode) {
//        super.startActivityForResult(intent, requestCode);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
//    }
}
