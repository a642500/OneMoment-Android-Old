package co.yishun.onemoment.app.ui.guide;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
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

    @Override
    public void onLastBtnClicked(View view) {
        getSharedPreferences(Config.PREFERENCE, MODE_PRIVATE).edit().putBoolean(Config.PREFERENCE_IS_FIRST_LAUNCH, false).apply();
        this.finish();
        RecordingActivity_.intent(this).start();
    }
}