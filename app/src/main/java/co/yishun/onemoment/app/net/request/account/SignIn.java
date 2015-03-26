package co.yishun.onemoment.app.net.request.account;

import android.util.Log;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.koushikdutta.async.future.FutureCallback;

import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2/15/15.
 */
public class SignIn extends Request<AccountResult> {
    public static final String TAG = LogUtil.makeTag(SignIn.class);
    private long phone;
    private String password;

    public SignIn setPassword(String password) {
        this.password = password;
        return this;
    }

    public SignIn setPhone(long phone) {
        this.phone = phone;
        return this;
    }


    @Override
    public String getUrl() {
        return Config.getUrlSignIn();
    }

    @Override
    public void setCallback(final FutureCallback<AccountResult> callback) {
        check();
        if (builder != null && callback != null) {
            builder.load(getUrl())
                    .setBodyParameter("key", key)
                    .setBodyParameter("phone", String.valueOf(phone))
                    .setBodyParameter("password", password)
                    .as(AccountResult.class).setCallback(callback);
            Log.i(TAG, "load sign in");

        }
    }

    @Override
    protected void check() {
        LogUtil.privateLog(TAG, "Check(): " + this.toString());
        if (!(AccountHelper.isValidPhoneNum(String.valueOf(phone))
                && AccountHelper.isValidPassword(password)
        )) {
            throw new IllegalStateException("A request with error data");
        }
    }

    @Override
    public String toString() {
        return "SignIn |"
                + " phone: " + phone
                + " password: " + password
                ;
    }

    /*

    POST /api/v2/signin

    * **Required** `key`
    * **Required** `phone`
    * **Required** `password `

     */

}
