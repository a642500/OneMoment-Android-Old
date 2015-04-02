package co.yishun.onemoment.app.ui;

import android.text.Editable;
import android.view.View;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by Carlos on 2015/4/2.
 */

@EActivity(R.layout.activity_set_password)
public class SetPasswordActivity extends BaseActivity {
    private static final String TAG = LogUtil.makeTag(SetPasswordActivity.class);
    @Extra
    String phone;

    String password = null;
    private String mPasswordAgain = null;

    @AfterTextChange
    void passwordEditTextAfterTextChanged(Editable password) {
        this.password = password.toString().trim();
    }

    @AfterTextChange
    void passwordAgainEditTextAfterTextChanged(Editable passwordAgain) {
        mPasswordAgain = passwordAgain.toString().trim();
    }

    @Click
    void nextBtnClicked(View view) {
        if (!checkPassword()) showNotification(R.string.setPasswordPasswordInvalid);
        else if (!checkPasswordAgain()) showNotification(R.string.setPasswordPasswordAgainInvalid);
        else {
            IntegrateInfoActivity_.intent(this)
                    .extra("phone", phone)
                    .extra("password", password)
                    .startForResult(IntegrateInfoActivity.REQUEST_PHONE);
        }
    }

    private boolean checkPassword() {
        return AccountHelper.isValidPassword(password);
    }

    private boolean checkPasswordAgain() {
        return password.equals(mPasswordAgain);
    }


}
