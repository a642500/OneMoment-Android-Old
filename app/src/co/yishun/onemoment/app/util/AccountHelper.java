package co.yishun.onemoment.app.util;

import android.accounts.Account;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Carlos on 2/15/15.
 */
public class AccountHelper {

    public static PasswordType checkPassword(@Nullable String pass) {
        if (TextUtils.isEmpty(pass)) return PasswordType.InvalidEmpty;
        if (pass.length() > 16) return PasswordType.InvalidTooLong;
        if (pass.length() < 6) return PasswordType.InvalidTooShort;
        return PasswordType.ValidMedium;
    }

    public static boolean isValidPassword(@Nullable String pass) {
        PasswordType type = checkPassword(pass);
        return PasswordType.ValidMedium == type || PasswordType.ValidStrong == type;
    }

    public static boolean isValidPhoneNum(@Nullable String phone) {
        try {
            long p = Long.valueOf(phone);
            return p > 100_0000_0000L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidVerificationCode(String code) {
        if (TextUtils.isEmpty(code)) return false;
        return true;
    }

    public enum PasswordType {
        /**
         * invalid password because it is empty
         */
        InvalidEmpty,
        /**
         * invalid password because of too short length
         */
        InvalidTooShort,
        /**
         * invalid password because of too long length
         */
        InvalidTooLong,
        /**
         * invalid password because of weak strength
         */
        InvalidTooWeak,
        /**
         * invalid password because of containing invalid character
         */
        InvalidCharacter,

        //valid:
        /**
         * valid password, which has enough strength
         */
        ValidMedium,
        /**
         * valid password, which has very good strength
         */
        ValidStrong
    }

    public static boolean saveAccountInfo(Account account) {
        if (account == null) {
            return false;
        }
        return true;
    }

    public static boolean updateSavedAccountInfo(Account account) {
        return true;
    }

    public static void deleteAccount() {
    }
}
