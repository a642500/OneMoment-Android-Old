package co.yishun.onemoment.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import co.yishun.onemoment.app.util.LogUtil;

/**
 * This service is to listen the network status and sync in the background.
 * <p>
 * Created by Carlos on 2/7/15.
 */
public class SyncService extends Service {
    // Object to use as a thread-safe lock
    private static final String TAG = LogUtil.makeTag(SyncService.class);
    public static final Object mSyncAdapterLock = new Object();
    private static SyncAdapter_ mSyncAdapter = null;

    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (mSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = SyncAdapter_.getInstance_(getApplicationContext());
            }
        }
    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        LogUtil.i(TAG, "SyncService onBind");
        return mSyncAdapter.getSyncAdapterBinder();
    }


}
