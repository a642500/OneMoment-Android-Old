package co.yishun.onemoment.app.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.PhoneVerification;
import co.yishun.onemoment.app.net.request.account.SignUp;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_sign_up)
public class SignUpActivity extends ActionBarActivity {

    private static final String TAG = LogUtil.makeTag(SignUpActivity.class);

    private String mPhoneNum;
    private String mVerificationCode;

    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phone) {
        mPhoneNum = text.toString();
    }

    @AfterTextChange(R.id.verificationCodeEditText)
    void onVerificationCodeChange(Editable text, TextView phone) {
        mVerificationCode = text.toString();
    }


    @Click
    void loginEntryBtnClicked(@NonNull View view) {

    }

    @Click
    void getVerificationCodeBtnClicked(@NonNull View view) {
        if (checkPhoneNum()) {
            ((PhoneVerification.SendSms) (new PhoneVerification.SendSms().with(this))).setPhone(mPhoneNum).setCallback((e, result) -> {
                if (result.getCode() == ErrorCode.SUCCESS) {
                    Toast.makeText(this, getString(R.string.signUpVerificationCodeSuccessToast), Toast.LENGTH_SHORT).show();
                    //TODO disable btn temporary
                    //TODO add handle sms
                } else {
                    Toast.makeText(this, getString(R.string.signUpVerificationCodeFailedToast), Toast.LENGTH_SHORT).show();
                }
            });
        } else
            Toast.makeText(this, "Your phone is invalid", Toast.LENGTH_SHORT).show();
    }

    @Click
    void nextBtnClicked(@NonNull View view) {
        if (checkPhoneNum() && checkVerificationCode())
            ((PhoneVerification.Verify) (new PhoneVerification.Verify().with(this))).setPhone(mPhoneNum).setVerifyCode(mVerificationCode).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(this, "verify failed!", Toast.LENGTH_SHORT).show();
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    Toast.makeText(this, "verify success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "verify failed!", Toast.LENGTH_SHORT).show();
                }
            });
        else
            Toast.makeText(this, "Your phone or verification code is wrong", Toast.LENGTH_SHORT).show();
    }

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(getString(R.string.signUpTitle));
        toolbar.setNavigationOnClickListener(v -> SignUpActivity.this.onBackPressed());
    }


    @UiThread
    void signUpSuccess(AccountResult result) {
        switch (result.getCode()) {
            case ErrorCode.SUCCESS:

                break;
            default:
                Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private boolean checkPhoneNum() {
        return AccountHelper.isValidPhoneNum(mPhoneNum);
    }

    private boolean checkVerificationCode() {
        return AccountHelper.isValidVerificationCode(mVerificationCode);
    }
}
