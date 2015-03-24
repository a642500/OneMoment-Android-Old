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
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.net.result.VerificationResult;
import co.yishun.onemoment.app.util.AccountHelper;
import com.koushikdutta.async.future.FutureCallback;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2/15/15.
 */
@EActivity(R.layout.login_layout)
public class LoginActivity extends ActionBarActivity {
    private String mPhoneNum;
    private String mVerificationCode;

    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phone) {
        mPhoneNum = text.toString();
    }

    @AfterTextChange(R.id.verificationEditText)
    void onVerificationCodeChange(Editable text, TextView phone) {
        mVerificationCode = text.toString();
    }


    @Click(R.id.loginBtn)
    void onLogin(@NonNull View loginBtn) {

    }

    @Click(R.id.nextBtn)
    void onNext(@NonNull View nextBtn) {
        if (checkPhoneNum() && checkVerificationCode())
            new PhoneVerification.Verify().setPhone(mPhoneNum).setVerifyCode(mVerificationCode).setCallback(new FutureCallback<VerificationResult>() {
                @Override
                public void onCompleted(Exception e, VerificationResult result) {

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
        toolbar.setTitle(getString(R.string.loginByPhoneTitle));

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