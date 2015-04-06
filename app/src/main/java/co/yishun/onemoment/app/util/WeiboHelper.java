package co.yishun.onemoment.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

/**
 * Created by Carlos on 2015/4/1.
 */
public class WeiboHelper {
    public static final String APP_KEY = "4070980764";
    public static final String APP_SECRET = "b264b30b5cae0497af3f7cb16aabe2c9";
    public static final String AUTH_REDIRECT_URL = "http://sns.whalecloud.com/sina2/callback";

    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";
    private static final String TAG = LogUtil.makeTag(WeiboHelper.class);
    public final SsoHandler ssoHandler;
    private final AuthInfo mAuthInfo;
    private final Activity mActivity;


    public WeiboHelper(Activity activity) {
        mActivity = activity;
        mAuthInfo = new AuthInfo(mActivity, APP_KEY, AUTH_REDIRECT_URL, SCOPE);
        ssoHandler = new SsoHandler(mActivity, mAuthInfo);
    }

    public void login(@NonNull WeiboLoginListener listener) {
        ssoHandler.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle values) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
                if (accessToken.isSessionValid()) {
//                    AccessTokenKeeper.writeAccessToken(mActivity.getApplicationContext(), accessToken);
                    listener.onSuccess(accessToken);
                } else {
                    String code = values.getString("code");
                    LogUtil.e(TAG, "weibo auth fail, code:" + code);
                    listener.onFail();
                }
            }

            @Override
            public void onWeiboException(WeiboException e) {
                LogUtil.e(TAG, "weibo auth exception", e);
                listener.onFail();
            }

            @Override
            public void onCancel() {
                listener.onCancel();
                LogUtil.e(TAG, "weibo auth cancel");
            }
        });
    }

    public interface WeiboLoginListener {
        void onSuccess(Oauth2AccessToken token);

        void onFail();

        void onCancel();
    }


    @Deprecated
    public static class AccessTokenKeeper {
        private static final String PREFERENCES_NAME = "com_onemoment_weibo";
        private static final String KEY_UID = "uid";
        private static final String KEY_ACCESS_TOKEN = "access_token";
        private static final String KEY_EXPIRES_IN = "expires_in";

        /**
         * save Token in SharedPreferences
         *
         * @param token Token
         */
        public static void writeAccessToken(@NonNull Context context, @NonNull Oauth2AccessToken token) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(KEY_UID, token.getUid());
            editor.putString(KEY_ACCESS_TOKEN, token.getToken());
            editor.putLong(KEY_EXPIRES_IN, token.getExpiresTime());
            editor.apply();
        }

        /**
         * read Token from SharedPreferences
         *
         * @return saved Token
         */
        public static Oauth2AccessToken readAccessToken(@NonNull Context context) {
            Oauth2AccessToken token = new Oauth2AccessToken();
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
            token.setUid(pref.getString(KEY_UID, ""));
            token.setToken(pref.getString(KEY_ACCESS_TOKEN, ""));
            token.setExpiresTime(pref.getLong(KEY_EXPIRES_IN, 0));
            return token;
        }

        /**
         * clear Token in SharedPreferences
         */
        public static void clear(@NonNull Context context) {
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND).edit().clear().apply();
        }
    }

}
