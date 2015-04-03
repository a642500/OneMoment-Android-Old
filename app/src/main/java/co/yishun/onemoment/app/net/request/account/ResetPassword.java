package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.DecodeUtil;
import co.yishun.onemoment.app.util.LogUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2/15/15.
 */
public class ResetPassword extends Request<AccountResult> {
    public static final String TAG = LogUtil.makeTag(SignIn.class);
    private String phone;
    private String password;

    public ResetPassword setPassword(String password) {
        this.password = password;
        return this;
    }

    public ResetPassword setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    @Override
    public String getUrl() {
        return Config.getUrlResetPassword() + "";//TODO add account id
    }

    @Override
    public void setCallback(final FutureCallback<AccountResult> callback) {
        check();
        if (builder != null && callback != null) {
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("phone", phone)
                        .setBodyParameter("password", password)
                        .asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), AccountResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
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
    /*

    POST /api/v2/reset_password

    * **Required** `key`
    * **Required** `phone`
    * **Required** `password`

     */
}
