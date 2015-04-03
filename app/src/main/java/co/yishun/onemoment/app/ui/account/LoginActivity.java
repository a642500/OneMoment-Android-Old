package co.yishun.onemoment.app.ui.account;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.SignIn;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2/15/15.
 */
@EActivity(R.layout.login_layout)
public class LoginActivity extends ToolbarBaseActivity {
    private static final String TAG = LogUtil.makeTag(LoginActivity.class);

    private String mPhoneNum;
    private String mPassword;
    @Extra
    String phone = "";

    @ViewById
    EditText phoneEditText;

    @AfterViews
    void initPhone() {
        phoneEditText.setText(phone);
    }

    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phone) {
        mPhoneNum = text.toString();
    }

    @AfterTextChange(R.id.passwordEditText)
    void onPasswordChange(Editable text, TextView phone) {
        mPassword = text.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
    }

    @Click
    void findPasswordBtnClicked(@NonNull View view) {

    }

    @Click(R.id.loginBtn)
    @Background
    void loginBtn(@NonNull View view) {
        if (checkPhoneNum() && checkPassword()) {
            showProgress();
            ((SignIn) (new SignIn().with(this))).setPhone(mPhoneNum).setPassword(mPassword).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification("Login failed! Please check your network.");
                } else {
                    switch (result.getCode()) {
                        case ErrorCode.SUCCESS:
                            showNotification("Login success");
                            AccountHelper.createAccount(this, result.getData());
                            setResult(RESULT_OK);
                            this.finish();
                            break;
                        case ErrorCode.ACCOUNT_DOESNT_EXIST:
                            showNotification("Your account or password is wrong.");
                        default:
                            showNotification("Login failed!");
                            break;
                    }
                }
                hideProgress();
            });
        } else
            showNotification("Your phone or password is invalid.");
    }

    private boolean checkPhoneNum() {
        return AccountHelper.isValidPhoneNum(mPhoneNum);
    }

    private boolean checkPassword() {
        return AccountHelper.isValidPassword(mPassword);
    }
}