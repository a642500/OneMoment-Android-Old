package co.yishun.onemoment.app.ui.guide;

import android.content.Intent;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
import com.viewpagerindicator.UnderlinePageIndicator;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * This activity is to guide user when first launched.
 * <p>
 * Created by Carlos on 2/7/15.
 */
@EActivity(R.layout.activity_guide)
public class GuideActivity extends ToolbarBaseActivity {
    @Extra
    boolean isFromSuggestion = false;

    @ViewById
    ViewPager guideViewPager;
    @ViewById
    UnderlinePageIndicator viewpagerIndicator;

    @Override public void startActivity(Intent intent) {
        overridePendingTransition(R.anim.act_fade_in, R.anim.act_fade_out);
        super.startActivity(intent);
    }

    @AfterViews void initViewPager() {
        toolbar.setVisibility(isFromSuggestion ? View.VISIBLE : View.GONE);
        guideViewPager.setAdapter(
                new FragmentPagerAdapter(getSupportFragmentManager()) {
                    int[] imageRes = {
                            R.drawable.guide_screen0,
                            R.drawable.guide_screen1,
                            R.drawable.guide_screen2,
                            R.drawable.guide_screen3,
                            R.drawable.guide_screen4
                    };

                    @Override
                    public android.support.v4.app.Fragment getItem(int position) {
                        return GuidePageFragment_.builder().imageRes(imageRes[position]).isLast(!isFromSuggestion && (position == imageRes.length - 1)).build();
                    }

                    @Override
                    public int getCount() {
                        return imageRes.length;
                    }
                }
        );
        viewpagerIndicator.setViewPager(guideViewPager);
        viewpagerIndicator.setSelectedColor(getResources().getColor(R.color.underlineIndicatorColor));
    }
}