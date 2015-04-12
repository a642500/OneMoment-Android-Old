package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.VerificationResult;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2/16/15.
 */
public abstract class PhoneVerification {
    public static class SendSms extends Request<VerificationResult> {
        private String phone;

        public SendSms setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        @Override
        protected String getUrl() {
            return Config.getUrlVerificationSendSms();
        }

        @Override
        public void setCallback(FutureCallback<VerificationResult> callback) {
            check(callback);
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("phone", phone)
                        .asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), VerificationResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void check(FutureCallback<VerificationResult> callback) {
            if (phone == null) {
                throw new IllegalArgumentException("phone is null!");
            }
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
        }
           /*

    POST /api/v2/send_verify_sms

    * **Required** `key `
    * **Required** `phone`

    **Return**
    ```
    {
        'code': 1,
        'msg': 'nickname not exists',
        'data': {
            'nickname': 'xxxx'
        }
    }
    ```
     */
    }

    public static class Verify extends Request<VerificationResult> {
        private String phone;
        private String verifyCode;

        public Verify setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Verify setVerifyCode(String verifyCode) {
            this.verifyCode = verifyCode;
            return this;
        }

        /*

            POST /api/v2/verify_phone

            * **Required** `key
            * **Required** `phone`
            * **Required** `verify_code`


            **Return**

            ```
            {
                'code': 1,
                'msg':  'phone verified successfully',
                'data': {
                    'phone': 'xxxxxx',
                    'verify_code': 'xxxx'
                }
            }
            ```
                 */
        @Override
        protected String getUrl() {
            return Config.getUrlVerificationVerify();
        }

        @Override
        public void setCallback(FutureCallback<VerificationResult> callback) {
            check(callback);
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("phone", phone)
                        .setBodyParameter("verify_code", verifyCode)
                        .asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), VerificationResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void check(FutureCallback<VerificationResult> callback) {
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
            if (phone == null) throw new IllegalStateException("null phone");
            if (verifyCode == null) throw new IllegalStateException("null verify code");
        }
    }
}
