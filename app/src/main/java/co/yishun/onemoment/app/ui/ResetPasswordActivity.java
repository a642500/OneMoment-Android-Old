package co.yishun.onemoment.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.ResetPassword;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2015/4/6.
 */
@EActivity(R.layout.activity_reset_password)
public class ResetPasswordActivity extends ToolbarBaseActivity {
    private static final String TAG = LogUtil.makeTag(ResetPasswordActivity.class);
    @Extra
    String phone;

    String password = null;
    private String mPasswordAgain = null;

    @ViewById
    EditText passwordEditText;
    @ViewById
    EditText passwordAgainEditText;

    @UiThread
    void shakePasswordEditText() {
        YoYo.with(Techniques.Shake).duration(getResources().getInteger(R.integer.defaultShakeDuration))
                .playOn(passwordEditText);
    }

    @UiThread
    void shakePasswordAgainEditText() {
        YoYo.with(Techniques.Shake).duration(getResources().getInteger(R.integer.defaultShakeDuration))
                .playOn(passwordAgainEditText);
    }

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
    void okBtnClicked(View view) {
        if (!checkPassword()) {
            shakePasswordEditText();
            showNotification(R.string.resetPasswordPasswordInvalid);
        } else if (!checkPasswordAgain()) {
            shakePasswordAgainEditText();
            showNotification(R.string.resetPasswordPasswordAgainInvalid);
        } else {
            showProgress();
            new ResetPassword().setPhone(phone).setPassword(password).with(this).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification(R.string.resetPasswordSaveFail);
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    showNotification(R.string.resetPasswordSaveSuccess);
                    setResult(Activity.RESULT_OK);
                    this.finish();
                } else {
                    switch (result.getErrorCode()) {
                        case ErrorCode.PASSWORD_FORMAT_ERROR:
                            showNotification(R.string.resetPasswordSaveFailFormat);
                            break;
                        default:
                            showNotification(R.string.resetPasswordSaveFail);
                            break;
                    }
                }
                hideProgress();
            });
        }
    }

    @Override
    public void finish() {
        new Handler().postDelayed(super::finish, 1000);
    }

    private boolean checkPassword() {
        return AccountHelper.isValidPassword(password);
    }

    private boolean checkPasswordAgain() {
        return password.equals(mPasswordAgain);
    }


}
