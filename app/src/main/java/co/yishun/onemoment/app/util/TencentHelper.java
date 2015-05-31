package co.yishun.onemoment.app.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yyz on 5/30/15.
 */
public class TencentHelper {
    public static final String APP_ID = "222222";// "1104574591";
    public static final String SCOPE = "get_user_info";
    private static final String TAG = LogUtil.makeTag(WeiboHelper.class);

    private final Tencent mTencent;


    public TencentHelper(Context context) {mTencent = Tencent.createInstance(APP_ID, context.getApplicationContext());}


    public void login(Activity activity, LoginListener loginListener) {
        mTencent.login(activity, SCOPE, createListener(loginListener));
    }

    public void login(Fragment fragment, LoginListener loginListener) {
        mTencent.login(fragment, SCOPE, createListener(loginListener));
    }

    public void getUserInfo(Context context, QQToken token) {
        UserInfo info = new UserInfo(context, token);
        info.getUserInfo(createListener(null));//TODO
    }

    private IUiListener createListener(LoginListener loginListener) {
        return new IUiListener() {
            @Override public void onComplete(Object o) {
                LogUtil.e(TAG, "tencent auth success");
                LogUtil.i(TAG, o.toString());

                /*
                {"access_token":"AA65A9B44143F26EC3D807B82E2CEB30","expires_in":"7776000","openid":"FD86E47D72173F60C8D1FB39FE0E5B24","pay_token":"9814D7483AA85184B955106BBDBAA349","ret":"0","pf":"desktop_m_qq-10000144-android-2002-","pfkey":"c6408025713ffec6b1f659dcd3424531","sendinstall":"0","auth_time":"1433066096308","page_type":"1"}
                 */
                try {
                    JSONObject jsonObject = new JSONObject(o.toString());
                    String token = jsonObject.getString("access_token");
                    String id = jsonObject.getString("openid");
                    long expiresIn = jsonObject.getLong("expires_in");
                    loginListener.onSuccess(new OAuthToken(id, token, expiresIn));
                } catch (JSONException e) {
                    e.printStackTrace();
                    loginListener.onFail();
                }
            }

            @Override public void onError(UiError uiError) {
                LogUtil.e(TAG, "tencent auth error: { " +
                                "message: " + uiError.errorMessage + ", " +
                                "detail: " + uiError.errorDetail + ", " +
                                "code: " + uiError.errorCode + "} "
                );
                loginListener.onFail();
            }

            @Override public void onCancel() {
                LogUtil.e(TAG, "tencent auth cancel");
                loginListener.onCancel();
            }
        };
    }
}
