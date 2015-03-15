package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.VerificationResult;
import com.koushikdutta.async.future.FutureCallback;

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
            check();
            if (callback != null) {
                builder.load(getUrl())
                        .setBodyParameter("phone", phone)
                        .as(VerificationResult.class).setCallback(callback);
            }
        }

        @Override
        protected void check() {
            if (phone == null) {
                throw new IllegalStateException("phone is null!");
            }
        }
           /*
    * 发送验证码(重发验证码也是这个接口)

    POST /api/v2/send_verify_sms

    * **Required** `key 接口使用秘钥`
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
                * 验证

            POST /api/v2/verify_phone

            * **Required** `key 接口使用秘钥`
            * **Required** `phone`
            * **Required** `verify_code`

            只有验证功能, 没有创建账户的功能

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
            check();
            if (callback != null) {
                builder.load(getUrl())
                        .setBodyParameter("phone", phone)
                        .setBodyParameter("verify_code", verifyCode)
                        .as(VerificationResult.class).setCallback(callback);
            }
        }

        @Override
        protected void check() {
            if (verifyCode == null || phone == null) {
                throw new IllegalStateException("error data request");
            }
        }
    }
}
