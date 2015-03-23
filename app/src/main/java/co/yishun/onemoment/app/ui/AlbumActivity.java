package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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

    Calendar today = Calendar.getInstance();

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
        toolbar.setTitle("Today");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
        toolbar.setSubtitle(format.format(new Date()));
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

    @AfterViews
    void initCalender() {
        Calendar thisMonth = Calendar.getInstance();
        int maxDay = thisMonth.getMaximum(Calendar.DAY_OF_MONTH);
        thisMonth.add(Calendar.YEAR, 1);
        thisMonth.set(Calendar.DAY_OF_MONTH, maxDay + 1);//TODO set time from begin to now

        Date today = new Date();
        calendarPickerView.init(today, thisMonth.getTime()).withSelectedDate(today);
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
