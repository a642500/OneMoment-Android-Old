package co.yishun.onemoment.app.ui.account;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.SignUp;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2015/4/2.
 */

@EActivity(R.layout.activity_set_password)
public class SetPasswordActivity extends ToolbarBaseActivity {
    private static final String TAG = LogUtil.makeTag(SetPasswordActivity.class);
    @Extra
    String phone;

    String password = null;
    private String mPasswordAgain = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
    }

    @AfterTextChange
    void passwordEditTextAfterTextChanged(Editable password) {
        this.password = password.toString().trim();
    }

    @AfterTextChange
    void passwordAgainEditTextAfterTextChanged(Editable passwordAgain) {
        mPasswordAgain = passwordAgain.toString().trim();
    }

    @Click
    @Background
    void nextBtnClicked(View view) {
        if (!checkPassword()) showNotification(R.string.setPasswordPasswordInvalid);
        else if (!checkPasswordAgain()) showNotification(R.string.setPasswordPasswordAgainInvalid);
        else {
            showProgress();
            (((SignUp.ByPhone) new SignUp.ByPhone().with(this))).
                    setPhone(phone).setPassword(password).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification(R.string.setPasswordNSignUpFail);
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    AccountHelper.createAccount(this, result.getData());
                    showNotification(R.string.setPasswordNSignUpSuccess);
                    setResult(Activity.RESULT_OK);
                    IntegrateInfoActivity_.intent(this)
                            .extra("phone", phone)
                            .extra("password", password)
                            .startForResult(IntegrateInfoActivity.REQUEST_PHONE);
                } else showNotification(R.string.setPasswordNSignUpFail);
                hideProgress();
            });


        }
    }

    @OnActivityResult(IntegrateInfoActivity.REQUEST_PHONE)
    void onResult(int resultCode) {
        switch (resultCode) {
            case RESULT_OK:
                this.finish();
                break;
            case RESULT_CANCELED:
                //has registered
                this.finish();
                break;
            default:
                break;
        }
    }

    private boolean checkPassword() {
        return AccountHelper.isValidPassword(password);
    }

    private boolean checkPasswordAgain() {
        return password.equals(mPasswordAgain);
    }


}
