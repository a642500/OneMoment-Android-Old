package co.yishun.onemoment.app.net.auth;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import co.yishun.onemoment.app.util.LogUtil;
import com.google.gson.Gson;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import static co.yishun.onemoment.app.net.auth.AccessTokenKeeper.KeeperType.Weibo;

/**
 * A proxy implement to auth by Sina weibo open api.
 * <p>
 * Created by Carlos on 2015/4/1.
 */
public class WeiboHelper implements AuthHelper {
    public static final String APP_KEY = "4070980764";
    public static final String APP_SECRET = "b264b30b5cae0497af3f7cb16aabe2c9";
    public static final String AUTH_REDIRECT_URL = "http://sns.whalecloud.com/sina2/callback";

    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";

    public static final String URL_GET_USER_INFO = "https://api.weibo.com/2/users/show.json?source=" + APP_KEY + "&uid=";
    public static final String URL_GET_USER_INFO_PART = "&access_token=";
    private static final String TAG = LogUtil.makeTag(WeiboHelper.class);
    public final SsoHandler ssoHandler;
    private final Activity mActivity;

    public WeiboHelper(Activity activity) {
        mActivity = activity;
        AuthInfo mAuthInfo = new AuthInfo(mActivity, APP_KEY, AUTH_REDIRECT_URL, SCOPE);
        ssoHandler = new SsoHandler(mActivity, mAuthInfo);
    }

    @Override
    public UserInfo getUserInfo(@NonNull OAuthToken token) {
        // Sina don't provide relational api in java sdk, we should get user info by web api
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(URL_GET_USER_INFO + token.getId() + URL_GET_USER_INFO_PART + token.getToken()).get().build();

        try {
            Response response = client.newCall(request).execute();
            return new Gson().fromJson(response.body().string(), UserInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void login(@NonNull LoginListener listener) {
        LogUtil.i(TAG, "start weibo login");
        ssoHandler.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle values) {
                Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
                if (accessToken.isSessionValid()) {
                    AccessTokenKeeper.which(Weibo).writeAccessToken(
                            mActivity.getApplicationContext(), OAuthToken.from(accessToken)
                    );
                    listener.onSuccess(OAuthToken.from(accessToken));
                    LogUtil.e(TAG, "weibo auth success");
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
                LogUtil.e(TAG, "weibo auth cancel");
                listener.onCancel();
            }
        });
    }
}
