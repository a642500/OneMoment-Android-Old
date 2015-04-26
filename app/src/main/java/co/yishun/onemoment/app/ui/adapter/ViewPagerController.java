package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.AlbumController;
import co.yishun.onemoment.app.ui.view.viewpager.JazzyViewPager;
import co.yishun.onemoment.app.util.LogUtil;

import java.util.Calendar;

/**
 * Created by Carlos on 2015/4/4.
 */
public class ViewPagerController extends PagerAdapter implements AlbumController, ViewPager.OnPageChangeListener {
    private static final String TAG = LogUtil.makeTag(ViewPagerController.class);
    private int middleInt = Integer.MAX_VALUE / 2;
    private ViewHolder holder[] = new ViewHolder[3];
    private int centerPageHolderIndex = 1;
    private int centerPagePosition = middleInt;
    private JazzyViewPager viewPager;
    private Context context;

    public int getTodayIndex() {
        return middleInt;
    }

    public ViewPagerController(Context context, JazzyViewPager viewPager) {
        this.context = context;
        this.viewPager = viewPager;

        for (int i = 0; i < 3; i++) {
            View view = LayoutInflater.from(context).inflate(R.layout.calendar, null);
            ViewHolder viewHolder = new ViewHolder(view);
            holder[i] = viewHolder;
        }
        viewPager.setAdapter(this);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(middleInt);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    private int getRelativePosition(int position) {
        return position - middleInt;
    }

    @Override
    public Object instantiateItem(ViewGroup parent, final int position) {
        ViewHolder currHolder = holder[(((position - centerPagePosition + centerPageHolderIndex) % 3) + 3) % 3];
        parent.removeView(currHolder.view);
        parent.addView(currHolder.view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        currHolder.refresh(getRelativePosition(position));
        viewPager.setObjectForPosition(currHolder, position);
        return currHolder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object obj) {
        //has been removed, no need
//        container.removeView(viewPager.findViewFromObject(position));
    }


    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view.getTag() == obj;
    }

    @Override
    public void showTodayMonthCalendar() {
        //TODO handle better
    }

    @Override
    public void showNextMonthCalendar() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    @Override
    public void showPreviousMonthCalendar() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    private OnMonthChangeListener mListener;

    public void notifyUpdate() {
        for (int i = 0; i < holder.length; i++) {
            ViewHolder viewHolder = holder[i];
            viewHolder.refresh(viewHolder.oldOffset);
        }
    }

    @Override
    public void setOnMonthChangeListener(OnMonthChangeListener listener) {
        mListener = listener;
    }

    public void onMonthChange(Calendar calendar) {
        if (mListener != null) {
            mListener.onMonthChange(calendar);
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int old = centerPagePosition;
            centerPagePosition = viewPager.getCurrentItem();
            centerPageHolderIndex = (centerPagePosition - old + centerPageHolderIndex) % 3;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, centerPagePosition - middleInt);
            onMonthChange(calendar);
        }
    }

    public class ViewHolder {
        View view;
        GridView calendarGridView;
        CalendarAdapter adapter;
        int oldOffset;

        public ViewHolder(View view) {
            this.view = view;
            calendarGridView = (GridView) view.findViewById(R.id.calenderGrid);
            adapter = new CalendarAdapter(view.getContext());
            calendarGridView.setAdapter(adapter);
            view.setTag(this);
        }

        public void refresh(int offset) {
            oldOffset = offset;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, offset);
            adapter.setCalender(calendar);
        }
    }
}