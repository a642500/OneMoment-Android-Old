package co.yishun.onemoment.app.net.result;

import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.request.account.ResetPassword;
import co.yishun.onemoment.app.net.request.account.SignIn;
import co.yishun.onemoment.app.net.request.account.SignUp;

import java.io.Serializable;

/**
 * This class will be produced by Gson.
 * <p>
 * It can be used as the returned result of
 * <h1>{@link SignUp}</h1>
 * {@link SignUp.ByPhone}<br/>
 * {@link SignUp.ByWeiBo}<br/>
 * {@link SignUp.ByWeChat}, {@link }<br/>
 * <h1>{@link SignIn}</h1>
 * <h1>{@link ResetPassword}</h1>
 * <h1>{@link IdentityInfo}</h1>
 * {@link IdentityInfo.Get}<br/>
 * {@link IdentityInfo.Update}<br/>
 * <p>
 * Created by Carlos on 2/15/15.
 */
public class AccountResult implements Serializable {
    private int code;
    private String msg;
    private Data data;

    public static class Data implements Serializable {
        public Data() {
        }

        private String _id;
        private String phone;
        private boolean available;
        private String signup_ua;
        private int signup_time;
        private String signup_ip;
        private String signin_ua;
        private int signin_time;
        private String signin_ip;
        private String nickname;
        private String email;
        private boolean email_validated;
        private String introduction;
        private String avatar_url;
        private String weibo_uid;

        public String get_id() {
            return _id;
        }

        public String getPhone() {
            return phone;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getSignup_ua() {
            return signup_ua;
        }

        public int getSignup_time() {
            return signup_time;
        }

        public String getSignup_ip() {
            return signup_ip;
        }

        public String getSignin_ua() {
            return signin_ua;
        }

        public int getSignin_time() {
            return signin_time;
        }

        public String getSignin_ip() {
            return signin_ip;
        }

        public String getNickname() {
            return nickname;
        }

        public String getEmail() {
            return email;
        }

        public boolean isEmail_validated() {
            return email_validated;
        }

        public String getIntroduction() {
            return introduction;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public String getWeibo_uid() {
            return weibo_uid;
        }
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Data getData() {
        return data;
    }
}
