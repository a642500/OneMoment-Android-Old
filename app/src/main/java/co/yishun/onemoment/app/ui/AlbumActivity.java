package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.LogUtil;
import com.squareup.timessquare.CalendarPickerView;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@EActivity(R.layout.activity_album)
public class AlbumActivity extends ActionBarActivity {

    @ViewById
    CalendarPickerView calendarPickerView;

    @AfterViews
    @Deprecated
    void initCalender() {
        showThisMonth();
    }

    /**
     * fresh calender by {@link AlbumActivity#displayedMonth}
     */
    @Deprecated
    private void showMonthCalender() {
        Pair<Date, Date> mon = getFirstAndLastDayOf(displayedMonth);
        calendarPickerView.init(mon.first, mon.second);
    }

    @Fun
    @Deprecated
    private Pair<Date, Date> getFirstAndLastDayOf(Calendar anyday) {
        Calendar min = (Calendar) displayedMonth.clone();
        Calendar max = (Calendar) displayedMonth.clone();
        min.set(Calendar.DAY_OF_MONTH, displayedMonth.getActualMinimum(Calendar.DAY_OF_MONTH));
        max.set(Calendar.DAY_OF_MONTH, displayedMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new Pair<>(min.getTime(), max.getTime());
    }

    @Deprecated
    void showNextMonthCalender() {
        displayedMonth.add(Calendar.MONTH, 1);
        showMonthCalender();
    }

    @Deprecated
    void showPreviewMonthCalender() {
        displayedMonth.add(Calendar.MONTH, -1);
        showMonthCalender();
    }

    @Deprecated
    void showThisMonth() {
        Calendar calendar = Calendar.getInstance();
        displayedMonth.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        displayedMonth.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        showMonthCalender();
    }


    @Fun
    @Click
    void backToToday(View view) {
        showThisMonth();
    }

    @Fun
    @Click
    void nextMonthBtn(View view) {
        mAdapter.showNextMonthCalender();
    }

    @Fun
    @Click
    void previousMonthBtn(View view) {
        mAdapter.showPreviewMonthCalender();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.albumTitle));
        toolbar.setTitleTextColor(getResources().getColor(R.color.textColorCalenderTitle));
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
        toolbar.setSubtitle(format.format(new Date()));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.textColorCalenderSubtitle));
    }

    @ViewById
    GridView calenderGrid;

    @ViewById
    TextView monthTextView;

    private CalenderAdapter mAdapter;

    @AfterViews
    void initCalenderGrid() {
        mAdapter = new CalenderAdapter(getLayoutInflater(), monthTextView);
        calenderGrid.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (item.getItemId()) {
            case R.id.action_login:
                LoginActivity_.intent(this).start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private final Calendar displayedMonth = Calendar.getInstance();

    @Fun
    @Click
    void shootingBtn(View view) {
        RecordingActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT).start();
    }

    @Fun
    @Click
    void replayBtn(View view) {

    }

    @Fun
    @Click
    void syncBtn(View view) {

    }


    static class CalenderAdapter extends BaseAdapter {
        private static final String TAG = LogUtil.makeTag(CalenderAdapter.class);
        private final LayoutInflater mInflater;
        private final Calendar mCalender;
        private final TextView mUpdateMonthTextView;


        public CalenderAdapter(LayoutInflater inflater, Calendar calendar, TextView updateMonthTextView) {
            mInflater = inflater;
            mCalender = calendar;
            this.mUpdateMonthTextView = updateMonthTextView;
            updateMonthTextView();
        }

        public CalenderAdapter(LayoutInflater mInflater, TextView updateMonthTextView) {
            this.mInflater = mInflater;
            mCalender = Calendar.getInstance();
            mUpdateMonthTextView = updateMonthTextView;
            updateMonthTextView();
        }

        public void showPreviewMonthCalender() {
            mCalender.add(Calendar.MONTH, -1);
            notifyDataSetInvalidated();
        }

        public void showNextMonthCalender() {
            mCalender.add(Calendar.MONTH, 1);
            notifyDataSetInvalidated();
        }

        @Override
        public void notifyDataSetChanged() {
            updateMonthTextView();
            super.notifyDataSetChanged();
        }

        @Fun
        private void updateMonthTextView() {
            mUpdateMonthTextView.setText(mCalender.get(Calendar.MONTH) + 1 + mInflater.getContext().getString(R.string.albumMonthTitle));
        }

        @Override
        public void notifyDataSetInvalidated() {
            updateMonthTextView();
            super.notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return getMaxDayOf(mCalender) + getOffsetOfDay();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Fun
        private int getDayOfWeekOfFirstDayOf(Calendar anyday) {
            Calendar calendar = (Calendar) anyday.clone();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int a = calendar.get(Calendar.DAY_OF_WEEK);
            Log.d(TAG, String.valueOf(a));
            return a;
        }

        @Fun
        private int getMaxDayOf(Calendar anyday) {
            int max = anyday.getActualMaximum(Calendar.DAY_OF_MONTH);
            return max;
        }

        private int getOffsetOfDay() {
            return getDayOfWeekOfFirstDayOf(mCalender) - 1 + WEEK_TITLE_RES.length;
        }

        @Override
        public long getItemId(int position) {
            return position - getOffsetOfDay() + 1;
        }

        public static final int[] WEEK_TITLE_RES = new int[]{
                R.string.albumWeekTitleSun,
                R.string.albumWeekTitleMon,
                R.string.albumWeekTitleTue,
                R.string.albumWeekTitleWed,
                R.string.albumWeekTitleThu,
                R.string.albumWeekTitleFri,
                R.string.albumWeekTitleSat
        };

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new CellView(parent).view;
            }
            CellView holder = (CellView) convertView.getTag();
            if (position < WEEK_TITLE_RES.length) {
                holder.view.setEnabled(false);
                holder.view.setVisibility(View.VISIBLE);
                holder.foregroundTextView.setText(WEEK_TITLE_RES[position]);
                holder.setViewHeightSlim();
                holder.backgroundImageView.setVisibility(View.INVISIBLE);
                holder.foregroundImageView.setVisibility(View.INVISIBLE);
                holder.view.setOnClickListener(null);
            } else if (position < getOffsetOfDay()) {
                holder.view.setVisibility(View.INVISIBLE);
                holder.view.setEnabled(false);
                holder.view.setOnClickListener(null);
            } else {
                holder.recoverViewHeight();
                holder.view.setVisibility(View.VISIBLE);
                holder.view.setEnabled(true);
                holder.backgroundImageView.setVisibility(View.INVISIBLE);
                holder.foregroundImageView.setVisibility(View.VISIBLE);
                //add background
                holder.foregroundTextView.setVisibility(View.VISIBLE);
                holder.foregroundTextView.setText(String.valueOf(position - getOffsetOfDay() + 1));
            }
            return holder.view;
        }

        class CellView {
            final View view;
            ImageView backgroundImageView;
            ImageView foregroundImageView;
            TextView foregroundTextView;

            public CellView(ViewGroup parent) {
                this.view = mInflater.inflate(R.layout.calender_cell, parent, false);
                backgroundImageView = (ImageView) view.findViewById(R.id.backgroundImageView);
                foregroundImageView = (ImageView) view.findViewById(R.id.foregroundImageView);
                foregroundTextView = (TextView) view.findViewById(R.id.foregroundTextView);
                view.setTag(this);
            }

            public void setViewHeightSlim() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, mInflater.getContext().getResources().getDisplayMetrics());
                view.setLayoutParams(params);
            }

            public void recoverViewHeight() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, mInflater.getContext().getResources().getDisplayMetrics());
                view.setLayoutParams(params);
            }
        }
    }
}
