package co.yishun.onemoment.app.sync;

import android.accounts.Account;
import android.content.*;
import android.os.Bundle;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 * <p/>
 * Created by Carlos on 3/10/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs, ContentResolver mContentResolver) {
        super(context, autoInitialize, allowParallelSyncs);
        this.mContentResolver = mContentResolver;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //TODO transfer data to server
        //see:http://developer.android.com/training/sync-adapters/creating-sync-adapter.html
    }
}
