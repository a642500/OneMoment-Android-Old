package co.yishun.onemoment.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

/**
 * This service is to listen the network status and sync in the background.
 * <p>
 * Created by Carlos on 2/7/15.
 */
@EService
public class SyncService extends Service {
    // Object to use as a thread-safe lock
    @Bean(value = SyncAdapter.class)
    static SyncAdapter mSyncAdapter = null;


    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }


}
