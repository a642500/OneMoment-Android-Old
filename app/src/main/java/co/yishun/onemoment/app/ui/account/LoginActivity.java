package co.yishun.onemoment.app.ui.account;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.SignIn;
import co.yishun.onemoment.app.ui.FindPasswordActivity_;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2/15/15.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends ToolbarBaseActivity {
    private static final String TAG = LogUtil.makeTag(LoginActivity.class);
    //    public static final int REQUEST_FIND_PASSWORD = 100;
    @Extra
    String phone = "";
    @ViewById
    EditText phoneEditText;
    private String mPhoneNum;
    private String mPassword;

    @AfterViews void initPhone() {
        phoneEditText.setText(phone);
    }

    @AfterTextChange(R.id.phoneEditText) void onPhoneChange(Editable text, TextView phone) {
        mPhoneNum = text.toString();
    }

    @AfterTextChange(R.id.passwordEditText) void onPasswordChange(Editable text, TextView phone) {
        mPassword = text.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
    }

    @Click void findPasswordBtnClicked(@NonNull View view) {
        FindPasswordActivity_.intent(this).extra("phone", mPhoneNum).start();
    }

    @Click(R.id.loginBtn)
    @Background void loginBtn(@NonNull View view) {
        if (checkPhoneNum() && checkPassword()) {
            showProgress();
            new SignIn().setPhone(mPhoneNum).setPassword(mPassword).with(this).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification(R.string.loginLoginFailNetwork);
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    showNotification(R.string.loginLoginSuccess);
                    AccountHelper.createAccount(this, result.getData());
                    setResult(RESULT_OK);
                    new Handler().postDelayed(super::finish, 1000);
                } else {
                    switch (result.getErrorCode()) {
                        case ErrorCode.ACCOUNT_DOESNT_EXIST:
                            showNotification(R.string.loginLoginFailAccountNotExist);
                            break;
                        case ErrorCode.PASSWORD_NOT_CORRECT:
                            showNotification(R.string.loginLoginFailPasswordError);
                            break;
                        default:
                            showNotification(R.string.loginLoginFail);
                            break;
                    }
                }
                hideProgress();
            });
        } else
            showNotification(R.string.loginLoginFailPasswordError);
    }

    private boolean checkPhoneNum() {
        return AccountHelper.isValidPhoneNum(mPhoneNum);
    }

    private boolean checkPassword() {
        return AccountHelper.isValidPassword(mPassword);
    }
}