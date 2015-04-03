package co.yishun.onemoment.app.ui.guide;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.RecordingActivity_;
import org.androidannotations.annotations.*;

/**
 * This activity is to guide user when first launched.
 * <p>
 * Created by Carlos on 2/7/15.
 */
@EActivity(R.layout.guide_activity)
public class GuideActivity extends Activity implements GuidePageFragment.OnLastBtnClickedListener {
    @ViewById
    ViewPager guideViewPager;

    @AfterViews
    void initViewPager() {
        guideViewPager.setAdapter(new PagerAdapter() {
            int[] imageRes = {
                    R.drawable.guide_screen0,
                    R.drawable.guide_screen1,
                    R.drawable.guide_screen2,
                    R.drawable.guide_screen3,
                    R.drawable.guide_screen4
            };

            @Override
            public int getCount() {
                return imageRes.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                return GuidePageFragment_.builder().imageRes(imageRes[position]).isLast(position == imageRes.length - 1).build();
            }
        });
    }

    @UiThread
    void onGuideFinish() {
        changeEnableStatus();
        RecordingActivity_.intent(this).start();
        this.finish();
    }

    /**
     * After user finished this guide, disable this activity, and enable {@link co.yishun.onemoment.app.ui.RecordingActivity} as default activity.
     */
    private void changeEnableStatus() {
        PackageManager packageManager = getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(this, RecordingActivity_.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        packageManager.setComponentEnabledSetting(new ComponentName(this, GuideActivity_.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onLastBtnClicked(View view) {
        onGuideFinish();
    }
}