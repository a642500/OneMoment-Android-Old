package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
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
    void initCalender() {
        showThisMonth();
    }

    /**
     * fresh calender by {@link AlbumActivity#displayedMonth}
     */
    private void showMonthCalender() {
        Pair<Date, Date> mon = getFirstAndLastDayOf(displayedMonth);
        calendarPickerView.init(mon.first, mon.second);
    }

    @Fun
    private Pair<Date, Date> getFirstAndLastDayOf(Calendar anyday) {
        Calendar min = (Calendar) displayedMonth.clone();
        Calendar max = (Calendar) displayedMonth.clone();
        min.set(Calendar.DAY_OF_MONTH, displayedMonth.getActualMinimum(Calendar.DAY_OF_MONTH));
        max.set(Calendar.DAY_OF_MONTH, displayedMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new Pair<>(min.getTime(), max.getTime());
    }

    void showNextMonthCalender() {
        displayedMonth.add(Calendar.MONTH, 1);
        showMonthCalender();
    }

    void showPreviewMonthCalender() {
        displayedMonth.add(Calendar.MONTH, -1);
        showMonthCalender();
    }

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

    @AfterViews
    void initCalenderGrid() {
        calenderGrid.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 31;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater in = getLayoutInflater();
                return new CellView(in, parent, position).view;
            }

            class CellView {
                final View view;
                ImageView backgroundImageView;
                ImageView foregroundImageView;
                TextView foregroundTextView;

                public CellView(LayoutInflater inflater, ViewGroup parent, int position) {
                    this.view = inflater.inflate(R.layout.calender_cell, parent, false);
                    backgroundImageView = (ImageView) view.findViewById(R.id.backgroundImageView);
                    foregroundImageView = (ImageView) view.findViewById(R.id.foregroundImageView);
                    foregroundTextView = (TextView) view.findViewById(R.id.foregroundTextView);

                    foregroundTextView.setText(String.valueOf(position));

                    view.setTag(this);
                }
            }
        });
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
}
