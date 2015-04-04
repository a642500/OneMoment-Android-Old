package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.ui.account.SignUpActivity_;
import co.yishun.onemoment.app.ui.adapter.ViewPagerController;
import co.yishun.onemoment.app.ui.guide.GuideActivity_;
import co.yishun.onemoment.app.ui.view.viewpager.JazzyViewPager;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import co.yishun.onemoment.app.util.WeiboHelper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import org.androidannotations.annotations.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


@EActivity(R.layout.activity_album)
public class AlbumActivity extends BaseActivity implements AlbumController.OnMonthChangeListener {

    private static final String TAG = LogUtil.makeTag(AlbumActivity.class);
    @ViewById
    Toolbar toolbar;
    @ViewById
    TextView calendarCurrentDay;
    @ViewById
    TextView calendarDayOfWeek;
    //    @ViewById
//    GridView calenderGrid;
    @ViewById
    TextView monthTextView;
    boolean justPressed = false;
    @ViewById
    TextView titleOfCalender;
    private AlbumController mController;
    private WeiboHelper mWeiboHelper = null;

    @Fun
    @Click
    void backToToday(View view) {
        mController.showTodayMonthCalendar();
    }

    @Fun
    @Click
    void nextMonthBtn(View view) {
        mController.showNextMonthCalendar();
    }

    @Fun
    @Click
    void previousMonthBtn(View view) {
        mController.showPreviousMonthCalendar();
    }

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.albumTitle));
        toolbar.setTitleTextColor(getResources().getColor(R.color.textColorCalenderTitle));
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
        toolbar.setSubtitle(format.format(new Date()));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.textColorCalenderSubtitle));
    }

    @AfterViews
    void initToday() {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        calendarCurrentDay.setText(day > 9 ? String.valueOf(day) : "0" + day);
        String[] weeks = getResources().getStringArray(R.array.dayOfWeek);
        calendarDayOfWeek.setText(weeks[today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY]);
    }

    @ViewById
    JazzyViewPager viewPager;

    @AfterViews
    void initViewPager() {
        viewPager.setTransitionEffect(JazzyViewPager.TransitionEffect.Tablet);
        mController = new ViewPagerController(this, viewPager);
        viewPager.setPageMargin(30);
    }
    //    @AfterViews
//    void initCalenderGrid() {
//        mAdapter = new CalenderAdapter(this);
//        calenderGrid.setAdapter(mAdapter);
//        mAdapter.setOnMonthChangeListener(this);
//        mAdapter.showTodayMonthCalender();
//    }

    @Override
    public void onMonthChange(Calendar calendar) {
        monthTextView.setText(calendar.get(Calendar.MONTH) + 1 + getString(R.string.albumMonthTitle));
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
            case R.id.action_identity:
                if (AccountHelper.isLogin(this)) IdentityInfoActivity_.intent(this).start();
                else mWeiboHelper = showLoginDialog();
                break;
            case R.id.action_sync_settings:
                if (AccountHelper.isLogin(this)) {
                    Intent intent = new Intent(this, SyncSettingsActivity.class);
                    startActivity(intent);
                } else mWeiboHelper = showLoginDialog();
                break;
            case R.id.action_rate:
                rate();
                break;
            case R.id.action_help:
                GuideActivity_.intent(this).extra("isFromSuggestion", true).start();
                break;
            case R.id.action_about:
                AboutActivity_.intent(this).start();
                break;
            default:

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mWeiboHelper != null) {
            mWeiboHelper.ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    @Fun
    @Click
    void shootingBtn(View view) {
        onBackPressed();
    }

    @Fun
    @Click
    void replayBtn(View view) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
    }

    @Fun
    @Click
    void syncBtn(View view) {
        if (!AccountHelper.isLogin(this)) {
            showLoginDialog();
            return;
        }
        if (!justPressed) {
            AccountHelper.syncAtOnce(this);
            justPressed = true;
            delayEnableSyncBtn();
        }
        showNotification("syncing...");
    }

    @UiThread(delay = 10 * 1000)
    void delayEnableSyncBtn() {
        justPressed = true;
    }

    @AfterViews
    void setOneMomentCount() {
        try {
            titleOfCalender.setText(String.valueOf(OpenHelperManager.getHelper(this, MomentDatabaseHelper.class).getDao(Moment.class).countOf()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public WeiboHelper showLoginDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this).customView(R.layout.dialog_login, false).backgroundColorRes(R.color.bgLoginDialogColor).build();
        View view = dialog.getCustomView();
        view.findViewById(R.id.loginByPhoneBtn).setOnClickListener(v -> {
            SignUpActivity_.intent(this).start();
            dialog.dismiss();
        });
        final WeiboHelper helper = new WeiboHelper(this);
        view.findViewById(R.id.loginByWeiboBtn).setOnClickListener(v -> {
            helper.login(new WeiboHelper.WeiboLoginListener() {
                @Override
                public void onSuccess(Oauth2AccessToken token) {
                    Toast.makeText(AlbumActivity.this, AlbumActivity.this.getString(R.string.weiboLoginSuccess), Toast.LENGTH_SHORT).show();
                    //TODO use token to sign up
                    dialog.dismiss();
                }

                @Override
                public void onFail() {
                    Toast.makeText(AlbumActivity.this, AlbumActivity.this.getString(R.string.weiboLoginFail), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(AlbumActivity.this, AlbumActivity.this.getString(R.string.weiboLoginCancel), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            dialog.dismiss();
        });
        dialog.show();
        return helper;
    }

    private void rate() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    //    @Override
//    public void startActivity(Intent intent) {
//        super.startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
//    }
//
//    @Override
//    public void startActivityForResult(Intent intent, int requestCode) {
//        super.startActivityForResult(intent, requestCode);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
//    }
}
