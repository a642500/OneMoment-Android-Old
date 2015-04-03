package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.builder.Builders;

import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2/15/15.
 */
public abstract class IdentityInfo {
    public static class Get extends Request<AccountResult> {
        @Override
        protected String getUrl() {
            return Config.getUrlIdentityInfoGet() + AccountHelper.getIdentityInfo(mContext).get_id();
        }

        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check();
            if (builder != null && callback != null) {
                try {
                    builder.load(getUrl())
                            .setBodyParameter("key", key).asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), AccountResult.class))).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void check() {
            //do nothing
        }
    /*

    GET /api/v2/account/<account_id>?key=<key>

    * **Required** `key
    * **Required** `<account_id>

    */
    }

    public static class Update extends Request<AccountResult> {
        private String nickname;
        private String introduction;
        private String gender;
        private String avatarUrl;
        private String location;
        private String mKey;
        private String mValue;

        public Update setNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Update set(String key, String value) {
            this.mKey = key;
            this.mValue = value;
            return this;
        }

        public Update setIntroduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public Update setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Update setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Update setLocation(String location) {
            this.location = location;
            return this;
        }

        @Override
        protected String getUrl() {
            return Config.getUrlIdentityInfoUpdate() + AccountHelper.getIdentityInfo(mContext).get_id();
        }

        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check();
            if (callback != null && builder != null) {
                try {
                    Builders.Any.U u = builder.load(getUrl()).setBodyParameter("key", key);
                    if (nickname != null) u.setBodyParameter("nickname", nickname);
                    if (introduction != null) u.setBodyParameter("introduction", introduction);
                    if (gender != null) u.setBodyParameter("gender", gender);
                    if (avatarUrl != null) u.setBodyParameter("avatar_url", avatarUrl);
                    if (location != null) u.setBodyParameter("location", location);
                    if (mValue != null && mKey != null) u.setBodyParameter(mKey, mValue);
                    u.asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), AccountResult.class))).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected void check() {
            //do nothing
        }
    /*

    PUT /api/v2/account/<account_id>
    POST /api/v2/update_account/<account_id>

    * **Required** `key `
    * **Required** `<account_id> `
    *  *Optional*  `nickname`
    *  *Optional*  `gender`
    *  *Optional*  `introduction`
    *  *Optional*  `location`
    *  *Optional*  `avatar_url`


     */
    }
}
