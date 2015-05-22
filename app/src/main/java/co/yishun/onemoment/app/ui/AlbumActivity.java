package co.yishun.onemoment.app.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.request.account.SignUp;
import co.yishun.onemoment.app.sync.SyncAdapter;
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
import com.j256.ormlite.stmt.Where;
import com.nispok.snackbar.Snackbar;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import org.androidannotations.annotations.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


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
    @ViewById
    FrameLayout viewPagerContainer;
    JazzyViewPager viewPager;
    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class)
    Dao<Moment, Integer> momentDao;
    @SystemService ConnectivityManager connectivityManager;
    /**
     * only after manual sync, it will notify user sync success.
     */
    boolean isManualSync = false;
    BroadcastReceiver mSyncDoneReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            LogUtil.i(TAG, "received sync done intent");
            if (isManualSync && intent.getBooleanExtra(SyncAdapter.SYNC_BROADCAST_EXTRA_IS_SUCCESS, false)) {
                LogUtil.i(TAG, "notify sync done");
                showNotification(R.string.albumSyncDoneSuccess);
                isManualSync = false;
            }
        }
    };
    private AlbumController mController;
    BroadcastReceiver mDownloadUpdateReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            LogUtil.i(TAG, "received download progress update intent");
            if (mController != null && intent.getIntExtra(SyncAdapter.SYNC_BROADCAST_EXTRA_THIS_PROGRESS, 0) == 100) {
                mController.notifyUpdate();
                setOneMomentCount();
            }
        }

    };
    BroadcastReceiver mRecoverUpdateReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            LogUtil.i(TAG, "received recover update intent");
            if (mController != null) {
                mController.notifyUpdate();
                setOneMomentCount();
            }
        }
    };
    private WeiboHelper mWeiboHelper = null;

    @Fun void backToToday() {
        viewPagerContainer.removeAllViews();
        viewPager = new JazzyViewPager(this);
        viewPager.setTransitionEffect(JazzyViewPager.TransitionEffect.Tablet);
        viewPagerContainer.addView(viewPager, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mController = new ViewPagerController(this, viewPager);
        mController.setOnMonthChangeListener(this);
        onMonthChange(Calendar.getInstance());
    }
    //    @AfterViews
//    void initCalenderGrid() {
//        mAdapter = new CalenderAdapter(this);
//        calenderGrid.setAdapter(mAdapter);
//        mAdapter.setOnMonthChangeListener(this);
//        mAdapter.showTodayMonthCalender();
//    }

    @Click @UiThread(delay = 300) void backToTodayClicked(View view) {
        backToToday();
    }

    @Fun
    @Click void nextMonthBtn(View view) {
        mController.showNextMonthCalendar();
    }

    @Fun
    @Click void previousMonthBtn(View view) {
        mController.showPreviousMonthCalendar();
    }

    @AfterViews void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.albumTitle));
        toolbar.setTitleTextColor(getResources().getColor(R.color.textColorCalenderTitle));
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
        toolbar.setSubtitle(format.format(new Date()));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.textColorCalenderSubtitle));
    }

    @AfterViews void initToday() {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        calendarCurrentDay.setText(day > 9 ? String.valueOf(day) : "0" + day);
        String[] weeks = getResources().getStringArray(R.array.dayOfWeek);
        calendarDayOfWeek.setText(weeks[today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY]);
    }

    @AfterViews void initViewPager() {
        backToToday();
//        viewPager.setTransitionEffect(JazzyViewPager.TransitionEffect.Tablet);
//        mController = new ViewPagerController(this, viewPager);
//        viewPager.setPageMargin(30);
    }

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
            case R.id.action_feedback:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{Config.FEEDBACK_MAIL});
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackSubject));
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedbackContent));
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    showNotification(R.string.feedbackFailNotification);
                }
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
    @Click void shootingBtn(View view) {
        onBackPressed();
    }

    @Fun
    @Click void replayBtn(View view) {
        try {

            List<Moment> momentList;
            Where<Moment, Integer> where = momentDao.queryBuilder().orderBy("timeStamp", true).where();
            if (AccountHelper.isLogin(this)) {
                momentList = where.eq("owner", "LOC").or().eq("owner", AccountHelper.getIdentityInfo(this).get_id()).query();
            } else {
                momentList = where.eq("owner", "LOC").query();
            }
            if (momentList == null || momentList.isEmpty()) {
                showNotification(R.string.albumReplayNoMoment);
            } else if (momentList.size() == 1) {
                PlayActivity_.intent(this).extra("moment", momentList.get(0)).extra("isReplayAll", true).start();
            } else {
                MultiPlayActivity_.intent(this).extra("moments", new ArrayList<>(momentList)).start();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        registerReceiver(mSyncDoneReceiver, new IntentFilter(SyncAdapter.SYNC_BROADCAST_DONE));
        registerReceiver(mDownloadUpdateReceiver, new IntentFilter(SyncAdapter.SYNC_BROADCAST_UPDATE_DOWNLOAD));
        registerReceiver(mRecoverUpdateReceiver, new IntentFilter(SyncAdapter.SYNC_BROADCAST_UPDATE_RECOVER));
    }

    @Override protected void onPause() {
        super.onPause();
        unregisterReceiver(mDownloadUpdateReceiver);
        unregisterReceiver(mSyncDoneReceiver);
        unregisterReceiver(mRecoverUpdateReceiver);
        isManualSync = false;//cancel notify sync done if user leaves.
    }

    @AfterInject void trySync() {
        if (AccountHelper.isLogin(this) && AccountHelper.isAutoSync(this)) {
            LogUtil.v(TAG, "try sync, start.");
            if (!AccountHelper.isOnlyWifiSyncEnable(this)) {
                AccountHelper.syncAtOnce(this);
            } else AccountHelper.syncAtOnceIgnoreNetwork(this);
        } else LogUtil.v(TAG, "try sync, not start");
    }

    @Fun
    @Click void syncBtn(View view) {
        if (!AccountHelper.isLogin(this)) {
            mWeiboHelper = showLoginDialog();
            return;
        }
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            sync(false);
        } else
            Snackbar.with(this).text(R.string.albumSyncAlertText).actionLabel(R.string.albumSyncAlertOk).actionColorResource(R.color.colorGreenBlue).actionListener(snackbar -> sync(true)).show(this);
    }

    void sync(boolean ignoreNetwork) {
        if (!justPressed) {
            isManualSync = true;
            if (ignoreNetwork) AccountHelper.syncAtOnceIgnoreNetwork(this);
            else AccountHelper.syncAtOnce(this);
            justPressed = true;
            delayEnableSyncBtn();
        } else LogUtil.v(TAG, "sync pressed in 10s, do nothing");
        showNotification(R.string.albumSyncText);
    }

    @Background(delay = 10 * 1000) void delayEnableSyncBtn() {
        justPressed = false;
    }

    @AfterViews void setOneMomentCount() {
        try {
            Dao<Moment, Integer> dao = OpenHelperManager.getHelper(this, MomentDatabaseHelper.class).getDao(Moment.class);
            if (AccountHelper.isLogin(this)) {
                titleOfCalender.setText(String.valueOf(dao.queryBuilder().where().eq("owner", "LOC").or().eq("owner", AccountHelper.getIdentityInfo(this).get_id()).countOf()));
            } else {
                titleOfCalender.setText(String.valueOf(dao.queryBuilder().where().eq("owner", "LOC").countOf()));
            }
            OpenHelperManager.releaseHelper();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public WeiboHelper showLoginDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this).customView(R.layout.dialog_login, false).backgroundColorRes(R.color.bgLoginDialogColor).autoDismiss(false).build();
        View view = dialog.getCustomView();
        view.findViewById(R.id.loginByPhoneBtn).setOnClickListener(v -> new Handler().postDelayed(() -> {
            SignUpActivity_.intent(this).start();
            dialog.dismiss();
        }, 150));
        final WeiboHelper helper = new WeiboHelper(this);
        view.findViewById(R.id.loginByWeiboBtn).setOnClickListener(
                v -> new Handler().postDelayed(
                        () -> {
                            helper.login(new WeiboHelper.WeiboLoginListener() {
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
                            });
                            dialog.dismiss();
                        }, 150
                )
        );
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
