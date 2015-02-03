package co.yishun.onemoment.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import co.yishun.library.fragmentwrapactivity.FragmentWrapActivity;
import co.yishun.onemoment.app.ui.RecordFragment;
import org.androidannotations.annotations.EActivity;

/**
 * MainActivity.
 * <p/>
 * Created by Carlos on 2/2/15.
 */
@EActivity
public class MainActivity extends FragmentWrapActivity {

    @Override
    public Fragment getWrappedFragment() {
        return new RecordFragment();
    }
}