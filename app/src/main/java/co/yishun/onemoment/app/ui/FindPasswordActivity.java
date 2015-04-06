package co.yishun.onemoment.app.ui;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.PhoneVerification;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_find_password)
public class FindPasswordActivity extends ToolbarBaseActivity {

    private static final String TAG = LogUtil.makeTag(FindPasswordActivity.class);
    public static final int REQUEST_SET_PASSWORD = 100;
    @Extra
    String phone = "";
    @ViewById
    EditText phoneEditText;
    @ViewById
    EditText verificationCodeEditText;

    private String mVerificationCode;

    @AfterViews
    void initPhone() {
        phoneEditText.setText(phone);
    }

    @ViewById
    Button getVerificationCodeBtn;

    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phoneText) {
        phone = text.toString();
    }

    @AfterTextChange(R.id.verificationCodeEditText)
    void onPasswordChange(Editable text, TextView passwordText) {
        mVerificationCode = text.toString();
    }


    @Click
    @Background
    void getVerificationCodeBtnClicked(@NonNull View view) {
        if (checkPhoneNum()) {
            countDown();
            ((PhoneVerification.SendSms) (new PhoneVerification.SendSms().with(this))).setPhone(phone).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification(R.string.signUpVerificationCodeFailedToast);
                    if (leftSeconds > 0) leftSeconds = 0;
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    showNotification(R.string.signUpVerificationCodeSuccessToast);
                } else {
                    showNotification(R.string.signUpVerificationCodeFailedToast);
                    if (leftSeconds > 0) leftSeconds = 0;
                }
            });
        } else {
            shakePhoneEditText();
            showNotification(R.string.signUpPhoneInvalidToast);
        }

    }

    int leftSeconds = 60;

    @UiThread(delay = 1000L)
    void countDown() {
        getVerificationCodeBtn.setEnabled(false);
        leftSeconds--;
        getVerificationCodeBtn.setText(getString(R.string.signUpVerificationRemainTimePrefix) + leftSeconds + getString(R.string.signUpVerificationRemainTimeSuffix));
        if (leftSeconds > 0) countDown();
        else {
            getVerificationCodeBtn.setEnabled(true);
            getVerificationCodeBtn.setText(R.string.signUpGetVerificationCode);
            leftSeconds = 60;
        }
    }

    @UiThread
    void shakePhoneEditText() {
        YoYo.with(Techniques.Shake).duration(getResources().getInteger(R.integer.defaultShakeDuration))
                .playOn(phoneEditText);
    }

    @UiThread
    void shakeVerificationEditText() {
        YoYo.with(Techniques.Shake).duration(getResources().getInteger(R.integer.defaultShakeDuration))
                .playOn(verificationCodeEditText);
    }

    @Click(R.id.nextBtn)
    void nextBtnClicked(@NonNull View view) {
        if (checkPhoneNum()) {
            if (checkVerificationCode()) {
                showProgress();
                ((PhoneVerification.Verify) (new PhoneVerification.Verify().with(this))).setPhone(phone).setVerifyCode(mVerificationCode).setCallback((e, result) -> {
                    if (e != null) {
                        e.printStackTrace();
                        showNotification(R.string.signUpVerifyFail);
                    } else switch (result.getCode()) {
                        case ErrorCode.SUCCESS:
                            showNotification(R.string.signUpVerifySuccess);
                            ResetPasswordActivity_.intent(this).extra("phone", phone).startForResult(REQUEST_SET_PASSWORD);
                            break;
                        case ErrorCode.PHONE_FORMAT_ERROR:
                            showNotification(R.string.signUpPhoneInvalidToast);
                            shakePhoneEditText();
                            break;
                        default:
                            showNotification(R.string.signUpVerifyFail);
                            break;
                    }
                    hideProgress();
                });
            } else {
                shakeVerificationEditText();
                showNotification(R.string.signUpVerificationCodeInvalidToast);
            }
        } else {
            shakePhoneEditText();
            showNotification(R.string.signUpPhoneInvalidToast);
        }
    }

    @OnActivityResult(REQUEST_SET_PASSWORD)
    void onResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            this.finish();
        }
    }

//    @UiThread
//    void signUpSuccess(AccountResult result) {
//        switch (result.getCode()) {
//            case ErrorCode.SUCCESS:
//
//                break;
//            default:
//                Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
//                break;
//        }
//    }

    private boolean checkPhoneNum() {
        return AccountHelper.isValidPhoneNum(phone);
    }

    private boolean checkVerificationCode() {
        return AccountHelper.isValidVerificationCode(mVerificationCode);
    }
}
