package co.yishun.onemoment.app.net.request.account;

import android.support.annotation.NonNull;
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
 * Bean to create a SignUp request
 * Created by Carlos on 2/9/15.
 */
public abstract class SignUp {
    private static final String TAG = LogUtil.makeTag(SignUp.class);

    public static class ByPhone extends Request<AccountResult> {
        /*

        POST /api/v2/signup

        * **Required** `key `
        * **Required** `phone `
        * **Required** `password `

        */
        private String phone;
        private String password;

        /**
         * @param phoneNum of the account
         */
        public ByPhone setPhone(String phoneNum) {
            this.phone = phoneNum;
            return this;
        }

        /**
         * @param password of the account
         */
        public ByPhone setPassword(@NonNull String password) {
            this.password = password;
            return this;
        }

        @Override
        public String toString() {
            return "SignUp ByPhone | " + "phone: " + phone + " password: " + password;
        }

        @Override
        protected String getUrl() {
            return Config.getUrlSignUpByPhone();
        }


        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check(callback);
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("phone", String.valueOf(phone))
                        .setBodyParameter("password", password)
                        .asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), AccountResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void check(FutureCallback<AccountResult> callback) {
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
            LogUtil.privateLog(TAG, "Check(): " + this.toString());
            if (!(AccountHelper.isValidPhoneNum(String.valueOf(phone))
                    && AccountHelper.isValidPassword(password)
            )) {
                throw new IllegalStateException("A request with error data");
            }
        }


    }

    public static class ByWeiBo extends Request<AccountResult> {
        private String uid;
        private String nickname;
        private String introduction;
        private String gender;
        private String avatarUrl;
        private String location;

        public ByWeiBo setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public ByWeiBo setNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public ByWeiBo setIntroduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public ByWeiBo setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public ByWeiBo setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public ByWeiBo setLocation(String location) {
            this.location = location;
            return this;
        }

        @Override
        public String getUrl() {
            return Config.getUrlSignUpByWeibo();
        }

        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check(callback);
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("uid", String.valueOf(uid))
                        .setBodyParameter("nickname", nickname)
                        .setBodyParameter("introduction", introduction)
                        .setBodyParameter("gender", gender)
                        .setBodyParameter("avatar_url", avatarUrl)
                        .setBodyParameter("location", location)
                        .asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), AccountResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void check(FutureCallback<AccountResult> callback) {
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
            LogUtil.privateLog(TAG, "Check(): " + this.toString());
        }

        @Override
        public String toString() {
            return "SignUp ByWeibo |"
                    + " uid: " + uid
                    + " nickname: " + nickname
                    + " introduction: " + introduction
                    + " gender: " + gender
                    + " avatar_url: " + avatarUrl
                    + " location: " + location
                    ;
        }
    }

        /*

        POST /api/v2/weibo_signup

        * **Required** `key 接口使用秘钥`
        * **Required** `uid 微博的uid`
        *  *Optional*  `nickname`
        *  *Optional*  `introduction`
        *  *Optional*  `gender`
        *  *Optional*  `avatar_url`
        *  *Optional*  `location`

         */


    public static class ByWeChat extends Request<AccountResult> {
        private String uid;
        private String nickname;
        private String introduction;
        private String gender;
        private String avatarUrl;
        private String location;

        public ByWeChat setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public ByWeChat setNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public ByWeChat setIntroduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public ByWeChat setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public ByWeChat setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public ByWeChat setLocation(String location) {
            this.location = location;
            return this;
        }

        @Override
        public String getUrl() {
            return Config.getUrlSignUpByWeChat();
        }

        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check(callback);
            builder.load(getUrl())
                    .setBodyParameter("key", key)
                    .setBodyParameter("uid", String.valueOf(uid))
                    .setBodyParameter("nickname", nickname)
                    .setBodyParameter("introduction", introduction)
                    .setBodyParameter("gender", gender)
                    .setBodyParameter("avatar_url", avatarUrl)
                    .setBodyParameter("location", location)
                    .as(AccountResult.class).setCallback(callback);
        }

        @Override
        protected void check(FutureCallback<AccountResult> callback) {
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
            LogUtil.privateLog(TAG, "Check(): " + this.toString());
        }

        @Override
        public String toString() {
            return "SignUp ByWeChat |"
                    + " uid: " + uid
                    + " nickname: " + nickname
                    + " introduction: " + introduction
                    + " gender: " + gender
                    + " avatar_url: " + avatarUrl
                    + " location: " + location
                    ;
        }

        /*

        POST /api/v2/weixin_signup

        * **Required** `key`
        * **Required** `uid`
        *  *Optional*  `nickname`
        *  *Optional*  `introduction`
        *  *Optional*  `gender`
        *  *Optional*  `avatar_url`
        *  *Optional*  `location`

         */
    }
}
