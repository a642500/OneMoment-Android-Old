package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.umeng.analytics.MobclickAgent;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Carlos on 2015/4/2.
 */
@EActivity
public class BaseActivity extends ActionBarActivity {

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
    }

    @UiThread
    public void showNotification(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    public void showNotification(int textRes) {
        showNotification(getString(textRes));
    }

    @UiThread
    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(this).theme(Theme.DARK).progress(true, 0).content(R.string.signUpLoading).build();
        }
        mProgressDialog.show();
    }

    @UiThread
    public void hideProgress() {
        mProgressDialog.hide();
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
