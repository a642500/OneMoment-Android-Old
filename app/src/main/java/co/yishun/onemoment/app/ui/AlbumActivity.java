package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.request.account.SignUp;
import co.yishun.onemoment.app.ui.account.SignUpActivity_;
import co.yishun.onemoment.app.ui.adapter.ViewPagerController;
import co.yishun.onemoment.app.ui.guide.GuideActivity_;
import co.yishun.onemoment.app.ui.view.viewpager.JazzyViewPager;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import co.yishun.onemoment.app.util.WeiboHelper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import org.androidannotations.annotations.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @ViewById
    FrameLayout viewPagerContainer;

    @Fun
    @Click
    void backToToday(View view) {
        viewPagerContainer.removeAllViews();
        viewPager = new JazzyViewPager(this);
        viewPager.setTransitionEffect(JazzyViewPager.TransitionEffect.Tablet);
        viewPagerContainer.addView(viewPager, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mController = new ViewPagerController(this, viewPager);
        mController.setOnMonthChangeListener(this);
        onMonthChange(Calendar.getInstance());
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

    JazzyViewPager viewPager;

    SparseArray storedPager = new SparseArray<>();

    @AfterViews
    void initViewPager() {
        backToToday(null);
//        viewPager.setTransitionEffect(JazzyViewPager.TransitionEffect.Tablet);
//        mController = new ViewPagerController(this, viewPager);
//        viewPager.setPageMargin(30);
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
                    SyncSettingsActivity_.intent(this).start();
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

    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class)
    Dao<Moment, Integer> momentDao;

    @Fun
    @Click
    void replayBtn(View view) {
        try {
            ArrayList<Moment> momentList = new ArrayList<>(15);
            momentList.addAll(momentDao.queryBuilder().limit(15).orderBy("timeStamp", true).query());
            if (momentList.size() >= 2) {
                MultiPlayActivity_.intent(this).extra("moments", momentList).start();
            } else if (momentList.size() == 1) {
                PlayActivity_.intent(this).extra("moment", momentList.get(0)).start();
            } else showNotification(R.string.albumReplayNoMoment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            mWeiboHelper = showLoginDialog();
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
        final MaterialDialog dialog = new MaterialDialog.Builder(this).customView(R.layout.dialog_login, false).backgroundColorRes(R.color.bgLoginDialogColor).autoDismiss(false).build();
        View view = dialog.getCustomView();
        view.findViewById(R.id.loginByPhoneBtn).setOnClickListener(v -> new Handler().postDelayed(() -> SignUpActivity_.intent(this).start(), 150));
        final WeiboHelper helper = new WeiboHelper(this);
        view.findViewById(R.id.loginByWeiboBtn).setOnClickListener(v -> new Handler().postDelayed(() -> helper.login(new WeiboHelper.WeiboLoginListener() {
            @Override
            public void onSuccess(Oauth2AccessToken token) {
                signUpByWeibo(token);
                dialog.dismiss();
            }

            @Override
            public void onFail() {
                showNotification(R.string.weiboLoginFail);
            }

            @Override
            public void onCancel() {
                showNotification(R.string.weiboLoginCancel);
            }
        }), 150));
        dialog.show();
        return helper;
    }

    @Background
    public void signUpByWeibo(Oauth2AccessToken token) {
        showNotification(R.string.weiboLoginAuthSuccess);
        showProgress();
        WeiboHelper.WeiBoInfo info = mWeiboHelper.getUserInfo(token);
        new SignUp.ByWeiBo().setUid(info.id).setGender(info.gender).setLocation(info.location).setNickname(info.name).setIntroduction(info.description).setAvatarUrl(info.avatar_large).with(this).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                showNotification(R.string.weiboLoginFail);
                hideProgress();
            } else if (result.getCode() == ErrorCode.SUCCESS) {
                AccountHelper.createAccount(this, result.getData());
                showNotification(R.string.weiboLoginSuccess);
                hideProgress();
            } else if (result.getErrorCode() == ErrorCode.WEIBO_UID_EXISTS) {
                //TODO login success at once, update info if not exist when in identity info act
                new IdentityInfo.Get().overrideUrlUID(token.getUid()).with(this).setCallback((e1, result1) -> {
                    if (e1 != null) {
                        e1.printStackTrace();
                        showNotification(R.string.weiboLoginFail);
                    } else if (result1.getCode() == ErrorCode.SUCCESS) {
                        AccountHelper.createAccount(this, result1.getData());
                        showNotification(R.string.weiboLoginSuccess);
                    } else {
                        showNotification(R.string.weiboLoginFail);
                    }
                    hideProgress();
                });
            }
        });
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
