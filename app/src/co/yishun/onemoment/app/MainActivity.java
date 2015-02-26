package co.yishun.onemoment.app;

import android.app.Fragment;
import co.yishun.library.fragmentwrapactivity.FragmentWrapActivity;
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
//        return new RecordFragment_();
        return null;
    }
}