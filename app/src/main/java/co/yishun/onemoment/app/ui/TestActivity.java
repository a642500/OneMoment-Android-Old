package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.LoginListener;
import co.yishun.onemoment.app.util.OAuthToken;
import co.yishun.onemoment.app.util.TencentHelper;
import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_test)
public class TestActivity extends ActionBarActivity {

    @ViewById
    Button testQQLoginBtn;
    @ViewById
    TextView qqTestResultTextView;

    @ViewById
    TextView qqInfoResultTextView;

    private OAuthToken mToken = null;

    private TencentHelper mHelper;


    @Click void testQQLoginBtnClicked() {
        qqTestResultTextView.setText("Waiting...");
        mHelper = new TencentHelper(this);
        mHelper.login(this, new LoginListener() {
            @Override public void onSuccess(OAuthToken token) {
                qqTestResultTextView.setText(token.getToken());
                mToken = token;
            }

            @Override public void onFail() {
                qqTestResultTextView.setText("fail");
            }

            @Override public void onCancel() {
                qqTestResultTextView.setText("cancel");
            }
        });
    }

    @Click void testQQInfoBtnClicked() {
        qqInfoResultTextView.setText("Waiting...");
        getInfo();
    }

    @Background void getInfo() {
        if (mToken == null || mHelper == null) {
            unableGetInfo();
        } else {
            TencentHelper.QQInfo info = mHelper.getUserInfo(this, mToken);
            System.out.println(info);
        }
    }

    @UiThread void unableGetInfo() {
        qqInfoResultTextView.setText("Please login first");
    }
}
