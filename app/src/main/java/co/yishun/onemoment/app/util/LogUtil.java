package co.yishun.onemoment.app.util;

import android.util.Log;

/**
 * This util is used to generate custom log.You can write log to some file on sdcard.
 * <p/>
 * Created by Carlos on 2/7/15.
 */
public class LogUtil {
    private final static String TAG = makeTag(LogUtil.class);

    public static String makeTag(Class classObject) {
        return classObject.getName();
    }

    public static int v(String tag, String msg) {
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }


    public static int i(String tag, String msg) {
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }


    public static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    /**
     * Send a protected log message.
     * This log must write into a encrypted log file, and it won't print in the Android System log.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int p(String tag, String msg) {
        throw new IllegalStateException("not implement");
    }

    /**
     * Send a protected log message and log the exception.
     * This log must write into a encrypted log file, and it won't print in the Android System log.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int p(String tag, String msg, Throwable tr) {
        throw new IllegalStateException("not implement");
    }

    /**
     * Send a private log message.
     * This log won't take effect when this is a release apk. You can only log password and phone this way.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int privateLog(String tag, String msg) {
        if (isDebug()) {
            Log.w(TAG, "private log is enabled!");
            return Log.w(tag, msg);
        }
        return -1;
    }

    /**
     * Send a private log message and log the exception.
     * This log won't take effect when this is a release apk. You can only log password and phone this way.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int privateLog(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            Log.w(TAG, "private log is enabled!");
            return Log.w(tag, msg, tr);
        }
        return -1;
    }

    public static boolean isDebug() {
        return true;//TODO change debug state when released.
    }

}
