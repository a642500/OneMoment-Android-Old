package co.yishun.onemoment.app.util;

import android.support.annotation.NonNull;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class OAuthToken {

    private final String id;
    private final String token;
    private final long expiresIn;

    public OAuthToken(@NonNull String id, @NonNull String token, @NonNull long expiresIn) {
        this.id = id;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public static OAuthToken from(Oauth2AccessToken weiboToken) {
        return new OAuthToken(
                weiboToken.getUid(),
                weiboToken.getToken(),
                weiboToken.getExpiresTime()
        );
    }

    public Oauth2AccessToken toWeiboToken() {
        Oauth2AccessToken oauth2AccessToken = new Oauth2AccessToken();
        oauth2AccessToken.setUid(id);
        oauth2AccessToken.setToken(token);
        oauth2AccessToken.setExpiresTime(expiresIn);
        return oauth2AccessToken;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
