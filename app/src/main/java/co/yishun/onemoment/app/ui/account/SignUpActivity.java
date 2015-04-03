package co.yishun.onemoment.app.ui.account;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.PhoneVerification;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_sign_up)
public class SignUpActivity extends ToolbarBaseActivity {

    public static final int REQUEST_LOGIN = 2;
    private static final String TAG = LogUtil.makeTag(SignUpActivity.class);
    private String phone;
    private String mVerificationCode;

    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phone) {
        this.phone = text.toString();
    }

    @AfterTextChange(R.id.verificationCodeEditText)
    void onVerificationCodeChange(Editable text, TextView phone) {
        mVerificationCode = text.toString();
    }

    @Click
    void loginEntryBtnClicked(@NonNull View view) {
        LoginActivity_.intent(this).extra("phone", phone).startForResult(REQUEST_LOGIN);
    }

    @Click
    @Background
    void getVerificationCodeBtnClicked(@NonNull View view) {
        if (checkPhoneNum()) {
            showProgress();
            ((PhoneVerification.SendSms) (new PhoneVerification.SendSms().with(this))).setPhone(phone).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification(R.string.signUpVerificationCodeFailedToast);
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    showNotification(R.string.signUpVerificationCodeSuccessToast);
                    //TODO disable btn temporary
                    //TODO add handle sms
                } else {
                    showNotification(R.string.signUpVerificationCodeFailedToast);
                }
                hideProgress();
            });
        } else
            showNotification("Your phone is invalid");
    }

    @Click
    @Background
    void nextBtnClicked(@NonNull View view) {
        if (checkPhoneNum() && checkVerificationCode()) {
            showProgress();
            ((PhoneVerification.Verify) (new PhoneVerification.Verify().with(this))).setPhone(phone).setVerifyCode(mVerificationCode).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification("verify failed!");
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    showNotification("verify success");
//                    startActivityForResult(new Intent(this, SetPasswordActivity.class).putExtra("phone", phone),IntegrateInfoActivity.REQUEST_PHONE);
                    SetPasswordActivity_.intent(this).extra("phone", phone).startForResult(IntegrateInfoActivity.REQUEST_PHONE);
//                    this.finish();
                } else {
                    showNotification("verify failed!");
                }
                hideProgress();
            });
        } else
            showNotification("Your phone or verification code is wrong");
    }

    @OnActivityResult(IntegrateInfoActivity.REQUEST_PHONE)
    void onSignUpResult(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                this.finish();
                break;
            default:
                break;
        }
    }

    @OnActivityResult(REQUEST_LOGIN)
    void onLoginResult(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                this.finish();
                break;
            default:
                break;
        }
    }

    @UiThread
    void signUpSuccess(AccountResult result) {
        switch (result.getCode()) {
            case ErrorCode.SUCCESS:

                break;
            default:
                showNotification("success");
                break;
        }
    }

    private boolean checkPhoneNum() {
        return AccountHelper.isValidPhoneNum(phone);
    }

    private boolean checkVerificationCode() {
        return AccountHelper.isValidVerificationCode(mVerificationCode);
    }
}
