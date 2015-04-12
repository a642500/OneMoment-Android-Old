package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.builder.Builders;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2/15/15.
 */
public abstract class IdentityInfo {
    public static class Get extends Request<AccountResult> {
        private String uid = null;

        @Override
        protected String getUrl() {
            return Config.getUrlIdentityInfoGet() + (uid == null ? AccountHelper.getIdentityInfo(mContext).get_id() : uid);
        }

        public IdentityInfo.Get overrideUrlUID(String uid) {
            this.uid = uid;
            return this;
        }
//    GET /api/v2/account/<account_id>?key=<key>

        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check(callback);
            OkHttpClient client = new OkHttpClient();
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(getUrl() + "?key=" + key).get().build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                    callback.onCompleted(e, null);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    callback.onCompleted(null, new Gson().fromJson(DecodeUtil.decode(response.body().string()), AccountResult.class));
                }
            });
        }

        @Override
        protected void check(final FutureCallback<AccountResult> callback) {
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
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
            check(callback);
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


        @Override
        protected void check(final FutureCallback<AccountResult> callback) {
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
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
