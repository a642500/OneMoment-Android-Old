package co.yishun.onemoment.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by yyz on 5/30/15.
 */
public class AccessTokenKeeper {
    private static final String KEY_UID = "uid";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private final String PREFERENCES_NAME;

    private AccessTokenKeeper(KeeperType type) {
        PREFERENCES_NAME = type.getPreferencesName();
    }

    public static AccessTokenKeeper which(KeeperType type) {
        return new AccessTokenKeeper(type);
    }

    /**
     * save Token in SharedPreferences
     *
     * @param token Token
     */
    public void writeAccessToken(@NonNull Context context, @NonNull OAuthToken token) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_UID, token.getId());
        editor.putString(KEY_ACCESS_TOKEN, token.getToken());
        editor.putLong(KEY_EXPIRES_IN, token.getExpiresIn());
        editor.apply();
    }

    /**
     * read Token from SharedPreferences
     *
     * @return saved Token
     */
    public OAuthToken readAccessToken(@NonNull Context context) {

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        return new OAuthToken(
                pref.getString(KEY_UID, ""),
                pref.getString(KEY_ACCESS_TOKEN, ""),
                pref.getLong(KEY_EXPIRES_IN, 0)
        );
    }

    /**
     * clear Token in SharedPreferences
     */
    public void clear(@NonNull Context context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND).edit().clear().apply();
    }

    public enum KeeperType {

        Weibo {
            @Override String getPreferencesName() {
                return "com_onemoment_weibo";
            }
        }, Tencent {
            @Override String getPreferencesName() {
                return "com_onemoment_tencent";
            }
        };

        abstract String getPreferencesName();
    }
}

