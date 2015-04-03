package co.yishun.onemoment.app.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.data.Contract;
import co.yishun.onemoment.app.net.result.AccountResult;

import java.io.*;

/**
 * Created by Carlos on 2/15/15.
 */
public class AccountHelper {
    public static final String ACCOUNT_TYPE = "co.yishun.onemoment.app";
    // The account name
//    public static final String ACCOUNT = "sync_account";
    private static final String TAG = LogUtil.makeTag(AccountHelper.class);
    private static Account mAccount;
    private static AccountResult.Data mIdentityInfo = null;

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

    public static boolean isValidNickname(String name) {
        return !TextUtils.isEmpty(name);
    }

    public static boolean isLogin(Context context) {
        String path = context.getDir(Config.IDENTITY_DIR, Context.MODE_PRIVATE) + "/" + Config.IDENTITY_INFO_FILE_NAME;
        return new File(path).exists();
    }

    public static Account createAccount(Context context, AccountResult.Data data) {
        // Create the account type and default account
        Account newAccount = new Account(data.getPhone(), ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            mAccount = newAccount;
            enableSync(context);
            saveIdentityInfo(context, data);
            return newAccount;
        } else {
            LogUtil.e(TAG, "The account exists or some other error occurred.");
            return null;
        }
    }

    public static void deleteAccount(Context context) {
        deleteIdentityInfo(context);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        accountManager.removeAccount(getAccount(context), null, null, null);
        mAccount = null;
    }

    public static boolean updateAccount(Context context, AccountResult.Data data) {
        deleteIdentityInfo(context);
        saveIdentityInfo(context, data);
        return true;
    }

    public static Account getAccount(Context context) {
        if (mAccount == null) {
            AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
            Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
            if (accounts.length > 0) mAccount = accounts[0];
        }
        return mAccount;
    }

    private static void loadInfo(Context context) {
        try {
            String path = context.getDir(Config.IDENTITY_DIR, Context.MODE_PRIVATE) + "/" + Config.IDENTITY_INFO_FILE_NAME;
            FileInputStream fin = null;
            fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            AccountResult.Data data = (AccountResult.Data) ois.readObject();
            mIdentityInfo = data;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean saveIdentityInfo(Context context, AccountResult.Data data) {

        try {
            String path = context.getDir(Config.IDENTITY_DIR, Context.MODE_PRIVATE) + "/" + Config.IDENTITY_INFO_FILE_NAME;
            LogUtil.i(TAG, "identity info path: " + path);
            FileOutputStream fout = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(data);
            oos.close();
            mIdentityInfo = data;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static void deleteIdentityInfo(Context context) {
        String path = context.getDir(Config.IDENTITY_DIR, Context.MODE_PRIVATE) + "/" + Config.IDENTITY_INFO_FILE_NAME;
        File info = new File(path);
        if (info.exists()) info.delete();
        mIdentityInfo = null;
    }

    public static AccountResult.Data getIdentityInfo(Context con) {
        if (mIdentityInfo == null) {
            loadInfo(con);
        }
        return mIdentityInfo;
    }

    private static void enableSync(Context context) {
        ContentResolver.addPeriodicSync(getAccount(context), Contract.AUTHORITY, new Bundle(), 60 * 10);
    }

    public static void syncAtOnce(Context context) {
        ContentResolver.requestSync(getAccount(context), Contract.AUTHORITY, new Bundle());
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


}
