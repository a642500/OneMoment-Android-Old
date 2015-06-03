package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.net.auth.LoginListener;
import co.yishun.onemoment.app.net.auth.OAuthToken;
import co.yishun.onemoment.app.net.auth.TencentHelper;
import co.yishun.onemoment.app.net.auth.UserInfo;
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
        mHelper.login(new LoginListener() {
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
            UserInfo info = mHelper.getUserInfo(mToken);
            System.out.println(info);
        }
    }

    @UiThread void unableGetInfo() {
        qqInfoResultTextView.setText("Please login first");
    }
}
