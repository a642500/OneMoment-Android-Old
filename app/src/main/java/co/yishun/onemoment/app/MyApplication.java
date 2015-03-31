package co.yishun.onemoment.app;

import android.app.Application;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import org.androidannotations.annotations.EApplication;
import quickutils.core.QuickUtils;

/**
 * Created by Carlos on 2/3/15.
 */
@EApplication
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QuickUtils.init(this);

    }

    public boolean isRelease = true;
}
