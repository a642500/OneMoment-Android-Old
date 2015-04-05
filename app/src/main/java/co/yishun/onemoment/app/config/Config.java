package co.yishun.onemoment.app.config;

import android.content.Context;
import android.util.Pair;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.net.request.sync.GetDomain;

/**
 * Created by Carlos on 2/15/15.
 */
public class Config {

    public static final String MIME_TYPE = "video/mp4";
    public static final String VIDEO_FILE_SUFFIX = ".mp4";
    public static final String URL_HYPHEN = "-";
    public static final String LONG_VIDEO_PREFIX = "long";
    public static final String AES_KEY = "QJBBNfrwp2oN4ZBwT9qZ4MGObN8y56bEydJj48L8xVs=";
    public static final String URL_VERIFICATION_SEND_SMS = "/api/v2/send_verify_sms";
    public static final String URL_VERIFICATION_VERIFY = "/api/v2/verify_phone";
    public static final String VIDEO_STORE_DIR = "moment";
    public static final String VIDEO_THUMB_STORE_DIR = "thumbs";
    public static final String IDENTITY_DIR = "identity";
    public static final String IDENTITY_INFO_FILE_NAME = "info";
    public static final String TIME_FORMAT = "yyyyMMdd";
    public static final String PREFERENCE = "preferences";
    public static final String PREFERENCE_IS_FIRST_LAUNCH = "is_first_launch";
    private final static String URL_MAIN = "http://test.yishun.co";
    private final static String URL_SIGN_UP_BY_PHONE = "/api/v2/signup";
    private final static String URL_SIGN_UP_BY_WEIBO = "/api/v2/weibo_signup";
    private final static String URL_SIGN_UP_BY_WECHAT = "/api/v2/weixin_signup";
    private final static String URL_SIGN_IN = "/api/v2/signin";
    private final static String URL_RESET_PASSWORD = "/api/v2/reset_password";
    private final static String URL_IDENTITY_INFO_GET = "/api/v2/account/";
    private final static String URL_IDENTITY_INFO_UPDATE = "/api/v2/update_account/";
    private final static String URL_CHECK_NICKNAME = "/api/v2/check_nickname";
    private final static String URL_TOKEN = "/api/v2/upload_token";
    private final static String URL_VIDEO_LIST = "/api/v2/videos/";
    private final static String URL_RESOURCE_DOMAIN = "/api/v2/resource_domain";
    private final static String URL_VIDEO_DELETE = "/api/v2/delete_video";


    private static final Pair<Integer, Integer> mCameraSize = new Pair<>(480, 480);
    private static final int FFMPEG_RAW_ID = R.raw.ffmpeg;
    private final static String PRIVATE_KEY = "jfio2q3de0ajd0923i9faoik209q0r83u4rjew";

    public static String getUrlDomain() {
        return URL_MAIN + URL_RESOURCE_DOMAIN;
    }

    public static String getUrlSignUpByPhone() {
        return URL_MAIN + URL_SIGN_UP_BY_PHONE;
    }

    public static String getUrlSignUpByWeibo() {
        return URL_MAIN + URL_SIGN_UP_BY_WEIBO;
    }

    public static String getUrlSignUpByWeChat() {
        return URL_MAIN + URL_SIGN_UP_BY_WECHAT;

    }

    public static String getUrlSignIn() {
        return URL_MAIN + URL_SIGN_IN;
    }

    public static String getUrlResetPassword() {
        return URL_MAIN + URL_RESET_PASSWORD;
    }

    public static String getUrlIdentityInfoGet() {
        return URL_MAIN + URL_IDENTITY_INFO_GET;
    }

    public static String getUrlIdentityInfoUpdate() {
        return URL_MAIN + URL_IDENTITY_INFO_UPDATE;
    }

    public static String getUrlCheckNickname() {
        return URL_MAIN + URL_CHECK_NICKNAME;
    }

    public static String getUrlVerificationSendSms() {
        return URL_MAIN + URL_VERIFICATION_SEND_SMS;
    }

    public static String getUrlVerificationVerify() {
        return URL_MAIN + URL_VERIFICATION_VERIFY;
    }

    public static String getUrlToken() {
        return URL_MAIN + URL_TOKEN;
    }

    public static String getUrlVideoList() {
        return URL_MAIN + URL_VIDEO_LIST;
    }

    public static String getPrivateKey() {
        return PRIVATE_KEY;
    }

    public static Pair<Integer, Integer> getDefaultCameraSize() {
        return mCameraSize;
    }

    public static int getFfmpegRawId() {
        return FFMPEG_RAW_ID;
    }

    private static String mResourceDomain = null;

    /**
     * <strong>block thread</strong>
     *
     * @param context
     * @return
     */
    private static String getResourceDomain(Context context) {
        if (mResourceDomain == null) {
            new GetDomain().setCallback((e, result) -> {
                if (e != null) e.printStackTrace();
                else mResourceDomain = result.getData().getDomain();
            });
        }
        return mResourceDomain;
    }

    public static String getResourceUrl(Context context) {
        String main = getResourceDomain(context);
        return (main.endsWith("/") ? main : main + "/");
    }

    public static String getUrlVideoDelete() {
        return URL_MAIN + URL_VIDEO_DELETE;
    }
}
