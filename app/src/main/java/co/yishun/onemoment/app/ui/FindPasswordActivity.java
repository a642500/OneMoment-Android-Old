package co.yishun.onemoment.app.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.PhoneVerification;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_find_password)
public class FindPasswordActivity extends ToolbarBaseActivity {

    private static final String TAG = LogUtil.makeTag(FindPasswordActivity.class);

    private String mPhoneNum;
    private String mVerificationCode;

    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phone) {
        mPhoneNum = text.toString();
    }

    @AfterTextChange(R.id.verificationCodeEditText)
    void onPasswordChange(Editable text, TextView phone) {
        mVerificationCode = text.toString();
    }


    @Click(R.id.getVerificationCodeBtn)
    void getVerificationCodeBtnClicked(@NonNull View view) {

    }

    @Click(R.id.nextBtn)
    void nextBtnClicked(@NonNull View view) {
        if (checkPhoneNum() && checkVerificationCode())
            ((PhoneVerification.Verify) (new PhoneVerification.Verify().with(this))).setPhone(mPhoneNum).setVerifyCode(mVerificationCode).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    //TODO handle result
                } else {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        else
            Toast.makeText(this, "Your phone or verification code is wrong", Toast.LENGTH_SHORT).show();
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
        return AccountHelper.isValidPhoneNum(mPhoneNum);
    }

    private boolean checkVerificationCode() {
        return AccountHelper.isValidVerificationCode(mVerificationCode);
    }
}
