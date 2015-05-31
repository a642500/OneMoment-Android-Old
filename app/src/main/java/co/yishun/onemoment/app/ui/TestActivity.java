package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.LoginListener;
import co.yishun.onemoment.app.util.OAuthToken;
import co.yishun.onemoment.app.util.TencentHelper;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_test)
public class TestActivity extends ActionBarActivity {

    @ViewById
    Button testQQLoginBtn;
    @ViewById
    TextView qqTestResultTextView;

    @Click void testQQLoginBtnClicked() {
        qqTestResultTextView.setText("Waiting...");
        new TencentHelper(this).login(this, new LoginListener() {
            @Override public void onSuccess(OAuthToken token) {
                qqTestResultTextView.setText(token.getToken());
            }

            @Override public void onFail() {
                qqTestResultTextView.setText("fail");
            }

            @Override public void onCancel() {
                qqTestResultTextView.setText("cancel");
            }
        });
    }
}
