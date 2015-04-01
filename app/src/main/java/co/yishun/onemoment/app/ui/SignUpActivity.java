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
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
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
    @Background
    void getVerificationCodeBtnClicked(@NonNull View view) {
        if (checkPhoneNum()) {
            showProgress();
            ((PhoneVerification.SendSms) (new PhoneVerification.SendSms().with(this))).setPhone(mPhoneNum).setCallback((e, result) -> {
                if (result.getCode() == ErrorCode.SUCCESS) {
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
            ((PhoneVerification.Verify) (new PhoneVerification.Verify().with(this))).setPhone(mPhoneNum).setVerifyCode(mVerificationCode).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification("verify failed!");
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    showNotification("verify success");
                } else {
                    showNotification("verify failed!");
                }
                hideProgress();
            });
        } else
            showNotification("Your phone or verification code is wrong");
    }

    @ViewById
    Toolbar toolbar;

    private MaterialDialog mProgressDialog;

    @UiThread
    void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new MaterialDialog.Builder(this).progress(true, 0).content(R.string.signUpLoading).build();
        }
        mProgressDialog.show();
    }

    @UiThread
    void showNotification(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void showNotification(int stringRes) {
        showNotification(getString(stringRes));
    }


    @UiThread
    void hideProgress() {
        mProgressDialog.hide();
    }

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
                showNotification("success");
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
