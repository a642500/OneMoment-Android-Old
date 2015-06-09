package co.yishun.onemoment.app.notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.RecordingActivity_;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.EBean;

import java.util.Calendar;
import java.util.Random;

/**
 * This is to send notification to shot onemoment everyday.
 * <p>
 * Created by yyz on 6/9/15.
 */
@EBean
public class EveryDayNotification extends BroadcastReceiver {


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
        Intent intent = new Intent(context, EveryDayNotification.class);

        long recurring = (60 * 60 * 24);  // in milliseconds
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), recurring, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        LogUtil.i(TAG, "schedule notification.");
    }

    /**
     * cancel everyday notification
     */
    public static void cancelScheduledNotification(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EveryDayNotification.class);
        am.cancel(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        LogUtil.i(TAG, "cancel scheduled notification.");
    }

    private void createNotification(Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large);

        int index = new Random().nextInt() % CONTENT_RES_IDS.length;
        index = index >= 0 ? index : index + CONTENT_RES_IDS.length;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setLargeIcon(bm).setSmallIcon(R.drawable.notification_small).setContentTitle(context.getString(R.string.notificationTitle)).setContentText(context.getString(CONTENT_RES_IDS[index])).setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);

        Intent resultIntent = new Intent(context, RecordingActivity_.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT));

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(ID, mBuilder.build());
    }

    @Override public void onReceive(Context context, Intent intent) {
        createNotification(context);
    }
}
