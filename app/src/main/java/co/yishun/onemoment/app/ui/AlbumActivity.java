package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final Calendar displayedMonth = Calendar.getInstance();
    ;

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
    void enterWorldBtn(View view) {

    }
}
