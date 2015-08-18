package co.yishun.onemoment.app.notification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.RecordingActivity_;
import co.yishun.onemoment.app.util.LogUtil;

/**
 * This is to send notification to shot onemoment everyday.
 * <p>
 * Created by yyz on 6/9/15.
 */
public class EveryDayNotification extends BroadcastReceiver {
    public static final String ACTION_ALARM_EVERYDAY = "co.yishun.onemoment.app.notification";
    private static final String PREFERENCE_NOTIFICATION = "notification";
    private static final String PREFERENCE_NOTIFICATION_NAME = "notification";


    private static final int ID = 1;
    private static final String TAG = "EveryDayNotification";
    private static final int CONTENT_RES_IDS[] = new int[]{
            R.string.notificationContent0,
            R.string.notificationContent1,
            R.string.notificationContent2,
            R.string.notificationContent3,
            R.string.notificationContent4,
            R.string.notificationContent5,
            R.string.notificationContent6,
            R.string.notificationContent7

    };

    /**
     * schedule everyday notification
     */
    public static void scheduleNotification(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_ALARM_EVERYDAY);

        if (BuildConfig.DEBUG) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 30);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            LogUtil.i(TAG, "schedule debug notification.");
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 14);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            LogUtil.i(TAG, "schedule notification.");
        }
    }

    /**
     * cancel everyday notification
     */
    public static void cancelScheduledNotification(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_ALARM_EVERYDAY);
        am.cancel(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        LogUtil.i(TAG, "cancel scheduled notification.");
    }

    public static boolean isEnableNotification(Context context) {
        return context.getSharedPreferences(PREFERENCE_NOTIFICATION_NAME, Context.MODE_PRIVATE).getBoolean(EveryDayNotification.PREFERENCE_NOTIFICATION, true);
    }

    public static void setEnableNotification(Activity activity, boolean enable) {
        activity.getSharedPreferences(PREFERENCE_NOTIFICATION_NAME, Activity.MODE_PRIVATE).edit().putBoolean(EveryDayNotification.PREFERENCE_NOTIFICATION, enable).apply();
        checkNotification(activity);
    }

    public static void checkNotification(Context context) {
        if (isEnableNotification(context))
            EveryDayNotification.scheduleNotification(context);
        else
            EveryDayNotification.cancelScheduledNotification(context);
    }

    private void createNotification(Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large);

        int index = new Random().nextInt() % CONTENT_RES_IDS.length;
        index = index >= 0 ? index : index + CONTENT_RES_IDS.length;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setLargeIcon(bm).setSmallIcon(R.drawable.notification_small).setAutoCancel(true).setContentTitle(context.getString(R.string.notificationTitle)).setContentText(context.getString(CONTENT_RES_IDS[index])).setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);

        Intent resultIntent = new Intent(context, RecordingActivity_.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(ID, mBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_ALARM_EVERYDAY)) {
            Log.i("EveryDayNotification", "ACTION_ALARM_EVERYDAY");
            createNotification(context);
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("EveryDayNotification", "ACTION_BOOT_COMPLETED");
            checkNotification(context);
        }
    }
}
