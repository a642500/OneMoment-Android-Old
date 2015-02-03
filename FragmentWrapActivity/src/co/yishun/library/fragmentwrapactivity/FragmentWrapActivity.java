package co.yishun.library.fragmentwrapactivity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * An abstract activity to use a Fragment as Main content.
 * <p/>
 * You should create an activity extend to this and implement {@link #getWrappedFragment()}.
 *
 * @ Created by Carlos on 2/3/15.
 */
public abstract class FragmentWrapActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());
        initLayout();
        setFragment();
    }

    /**
     * @return resource id of the container content view, it must contains {link@R.id.fragmentContainer}
     */
    public int getLayoutResID() {
        return R.layout.activity_fragment_wrap_default;
    }

    /**
     * init the view after {@link #setContentView(int)}
     */
    public void initLayout() {

    }

    private void setFragment() {
        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, getWrappedFragment()).commit();
    }

    /**
     * @return Fragment instance to wrap.
     */
    public abstract Fragment getWrappedFragment();
}