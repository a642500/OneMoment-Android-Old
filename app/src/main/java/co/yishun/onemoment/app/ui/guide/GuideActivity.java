package co.yishun.onemoment.app.ui.guide;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.RecordingActivity_;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
import com.viewpagerindicator.UnderlinePageIndicator;
import org.androidannotations.annotations.*;

/**
 * This activity is to guide user when first launched.
 * <p>
 * Created by Carlos on 2/7/15.
 */
@EActivity(R.layout.guide_activity)
public class GuideActivity extends ToolbarBaseActivity implements GuidePageFragment.OnLastBtnClickedListener {
    @Extra
    boolean isFromSuggestion = false;

    @ViewById
    ViewPager guideViewPager;
    @ViewById
    UnderlinePageIndicator viewpagerIndicator;

    @AfterViews
    void initViewPager() {
        toolbar.setVisibility(isFromSuggestion ? View.VISIBLE : View.GONE);
        guideViewPager.setAdapter(
                new FragmentPagerAdapter(getSupportFragmentManager()) {
                    @Override
                    public android.support.v4.app.Fragment getItem(int position) {
                        return GuidePageFragment_.builder().imageRes(imageRes[position]).isLast(!isFromSuggestion && (position == imageRes.length - 1)).build();
                    }

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
                }
        );
        viewpagerIndicator.setViewPager(guideViewPager);
        viewpagerIndicator.setSelectedColor(getResources().getColor(R.color.underlineIndicatorColor));
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