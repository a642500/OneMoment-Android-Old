package co.yishun.onemoment.app.config;

/**
 * Created by Carlos on 2/15/15.
 */
public class ErrorCode {
    public static final int SUCCESS = 1;
    public static final int FAIL = 0;

    public static final int INPUT_MISSING = -1;
    public static final int INPUT_ERROR = -2;
    public static final int SERVER_ERROR = -3;

    public static final int API_REQUEST_TOO_FEQUENT = -5;
    public static final int API_KEY_IS_MISSING = -6;
    public static final int API_KEY_ERROR = -7;

    public static final int NOT_AUTH_REQUEST = -8;
    public static final int CANNOT_WRITE_DATABASE = -9;

    public static final int PHONE_IS_MISSING = -10;
    public static final int PHONE_FORMAT_ERROR = -11;

    public static final int PASSWORD_IS_MISSING = -12;
    public static final int PASSWORD_NOT_CORRECT = -13;
    public static final int PASSWORD_FORMAT_ERROR = -14;

    public static final int FILENAME_IS_MISSING = -15;
    public static final int QINIU_DELETE_FAILED = -16;

    public static final int WEIBO_UID_IS_MISSING = -17;
    public static final int WEIBO_UID_FORMAT_ERROR = -18;
    public static final int WEIBO_UID_EXISTS = -19;

    public static final int ACCOUNT_EXISTS = -20;
    public static final int ACCOUNT_NOT_AVAILABLE = -21;
    public static final int ACCOUNT_ID_IS_MISSING = -22;
    public static final int ACCOUNT_DOESNT_EXIST = -23;

    public static final int WEIXIN_UID_IS_MISSING = -24;
    public static final int WEIXIN_UID_FORMAT_ERROR = -25;
    public static final int WEIXIN_UID_IS_EXISTS = -26;

    public static final int PHONE_VERIFIED = -27;
    public static final int PHONE_VERIFY_CODE_IS_MISSING = -28;
    public static final int PHONE_VERIFY_CODE_WRONG = -29;

    public static final int SMS_SEND_FAIL = -30;
    public static final int NICKNAME_FORMAT_ERROR = -32;
    public static final int DESCRIPTION_FORMAT_ERROR = -33;
    public static final int GENDER_FORMAT_ERROR = -34;
    public static final int LOCATION_FORMAT_ERROR = -35;
    public static final int AVATAR_URL_ERROR = -36;
    public static final int NICKNAME_EXISTS = -37;
    public static final int PHONE_NOT_VERIFIED = -38;
    public static final int WEIBO_UID_NOT_MATCH = -39;
    public static final int WEIXIN_UID_NOT_MATCH = -40;
    public static final int IOS_DEVICE_TOKEN_IS_MISSING = -41;
    public static final int ADMIN_DYNAMIC_PASSWORD_MISSING = -42;
    public static final int CAPTCHA_IS_MISSING = -43;
    public static final int UNKNOWN_ERROR = -44;
    public static final int DYNAMIC_PW_EXPIRED = -45;
    public static final int ACCOUNT_AUTH_ERROR = -46;

    public static final int WORLD_ID_MISSING = -47;
    public static final int WORLD_DESCRIPTION_MISSING = -48;
    public static final int WORLD_DOESNT_EXIST = -49;
    public static final int WORLD_LIKED = -50;

    public static final int CAPTCHA_ERROR = -51;
    public static final int REPORT_REASON_MISSING = -52;

    public static final int BANNER_IMAGE_URL_MISSING = -53;
    public static final int BANNER_HREF_MISSING = -54;
    public static final int BANNER_ID_MISSING = -55;

    public static final int NICKNAME_EMPTY = -56;
}
