package co.yishun.onemoment.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.request.account.SignIn;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import org.androidannotations.annotations.*;

/**
 * Created by Carlos on 2/15/15.
 */
@EActivity(R.layout.login_layout)
public class LoginActivity extends ActionBarActivity {
    private static final String TAG = LogUtil.makeTag(LoginActivity.class);

    private String mPhoneNum;
    private String mPassword;

    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phone) {
        mPhoneNum = text.toString();
    }

    @AfterTextChange(R.id.passwordEditText)
    void onPasswordChange(Editable text, TextView phone) {
        mPassword = text.toString();
    }


    @Click
    void findPasswordBtnClicked(@NonNull View view) {

    }

    @Click(R.id.loginBtn)
    void loginBtn(@NonNull View view) {
        if (checkPhoneNum() && checkPassword())
            ((SignIn) (new SignIn().with(this))).setPhone(Long.parseLong(mPhoneNum)).setPassword(mPassword).setCallback((e, result) -> {

                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show();
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
                    AccountHelper.saveIdentityInfo(result.getData(), this);
                } else {
                    Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show();
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
        toolbar.setNavigationOnClickListener(v -> LoginActivity.this.onBackPressed());
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

    private boolean checkPassword() {
        return AccountHelper.isValidPassword(mPassword);
    }

    public static void showLoginDialog(Context context) {
        final MaterialDialog dialog = new MaterialDialog.Builder(context).customView(R.layout.login_dialog, false).build();
        View view = dialog.getCustomView();
        view.findViewById(R.id.loginByPhoneBtn).setOnClickListener(v -> {
            LoginActivity_.intent(context).start();
            dialog.dismiss();
        });
        dialog.show();
    }
}