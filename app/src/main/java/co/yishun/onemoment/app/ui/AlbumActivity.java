package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.ui.account.SignUpActivity_;
import co.yishun.onemoment.app.ui.guide.GuideActivity_;
import co.yishun.onemoment.app.ui.view.viewpager.JazzyViewPager;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import co.yishun.onemoment.app.util.WeiboHelper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

interface OnMonthChangeListener {
    void onMonthChange(Calendar calendar);
}


@EActivity(R.layout.activity_album)
public class AlbumActivity extends BaseActivity implements OnMonthChangeListener {

    private static final String TAG = LogUtil.makeTag(AlbumActivity.class);
    private final Calendar displayedMonth = Calendar.getInstance();
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
    private CalenderAdapter mAdapter;
    private WeiboHelper mWeiboHelper = null;

    @Fun
    @Click
    void backToToday(View view) {
        mAdapter.showTodayMonthCalender();
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


//        @Fun
//        private void updateMonthTextView() {
//            mUpdateMonthTextView.setText(mCalender.get(Calendar.MONTH) + 1 + mInflater.getContext().getString(R.string.albumMonthTitle));
//        }

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
        CalendarPagerAdapter adapter = new CalendarPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(30);
    }

    private class CalendarPagerAdapter extends PagerAdapter {
        private int middleInt = Integer.MAX_VALUE / 2;

        public int getTodayIndex() {
            return middleInt;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        private Calendar getCalender(int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, getRelativePosition(position));
            return calendar;
        }

        private int getRelativePosition(int position) {
            return position - middleInt;
        }

        @Override
        public Object instantiateItem(ViewGroup parent, final int position) {
            View convertView = LayoutInflater.from(AlbumActivity.this).inflate(R.layout.calendar, parent, false);
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder(convertView);
            }
            viewHolder.adapter.setCalender(getCalender(position));
            parent.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            viewPager.setObjectForPosition(viewHolder, position);
            return viewHolder;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object obj) {
            container.removeView(viewPager.findViewFromObject(position));
        }


        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view.getTag() == obj;
        }

        public class ViewHolder {
            View view;
            GridView calendarGridView;
            CalenderAdapter adapter;

            public ViewHolder(View view) {
                this.view = view;
                calendarGridView = (GridView) view.findViewById(R.id.calenderGrid);
                adapter = new CalenderAdapter(view.getContext());
                calendarGridView.setAdapter(adapter);
                view.setTag(this);
            }
        }
    }

//    @ViewById
//    FlipView flipView;
//
//    @AfterViews
//    void initFlipView() {
//        FlipAdapter adapter = new FlipAdapter(this);
//        flipView.setAdapter(adapter);
//        flipView.flipTo(adapter.getTodayIndex());
//    }

    @Deprecated
    public static class FlipAdapter extends BaseAdapter {
        private final Context mContext;
        private int middleInt = 0
//                Integer.MAX_VALUE / 2
                ;

        public int getTodayIndex() {
            return middleInt;
        }

        public FlipAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return 10
//                    Integer.MAX_VALUE
                    ;
        }

        @Override
        public Object getItem(int position) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, getRelativePosition(position));
            return calendar;
        }

        public int getRelativePosition(int position) {
            return position - middleInt;
        }

        @Override
        public long getItemId(int position) {
            return position - middleInt;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.calendar, parent, false);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder(convertView);
            }
            viewHolder.adapter.setCalender((Calendar) getItem(position));
            return convertView;
        }

        public static class ViewHolder {
            View view;
            GridView calendarGridView;
            CalenderAdapter adapter;

            public ViewHolder(View view) {
                this.view = view;
                calendarGridView = (GridView) view.findViewById(R.id.calenderGrid);
                adapter = new CalenderAdapter(view.getContext());
                calendarGridView.setAdapter(adapter);
                view.setTag(this);
            }
        }
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
    @EBean
    static class CalenderAdapter extends BaseAdapter {
        public static final int[] WEEK_TITLE_RES = new int[]{
//                R.string.albumWeekTitleSun,
//                R.string.albumWeekTitleMon,
//                R.string.albumWeekTitleTue,
//                R.string.albumWeekTitleWed,
//                R.string.albumWeekTitleThu,
//                R.string.albumWeekTitleFri,
//                R.string.albumWeekTitleSat
        };
        private static final String TAG = LogUtil.makeTag(CalenderAdapter.class);
        private final Calendar mCalender;
        private final Context mContext;
        private OnMonthChangeListener onMonthChangeListener;


        public CalenderAdapter(Context context) {
            this.mContext = context;
            mCalender = Calendar.getInstance();
        }

        public void setCalender(Calendar calendar) {
            mCalender.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            mCalender.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            notifyDataSetInvalidated();
        }

        public void setOnMonthChangeListener(OnMonthChangeListener listener) {
            onMonthChangeListener = listener;
        }

        private void onChange(Calendar calendar) {
            if (onMonthChangeListener != null) {
                onMonthChangeListener.onMonthChange(calendar);
            }
        }

        public void showPreviewMonthCalender() {
            mCalender.add(Calendar.MONTH, -1);
            notifyDataSetInvalidated();
        }

        public void showNextMonthCalender() {
            mCalender.add(Calendar.MONTH, 1);
            notifyDataSetInvalidated();
        }

        public void showTodayMonthCalender() {
            Calendar today = Calendar.getInstance();
            mCalender.set(Calendar.YEAR, today.get(Calendar.YEAR));
            mCalender.set(Calendar.MONTH, today.get(Calendar.MONTH));
            notifyDataSetInvalidated();
        }

        @Override
        public void notifyDataSetChanged() {
            onChange(mCalender);
            super.notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetInvalidated() {
            onChange(mCalender);
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
            return calendar.get(Calendar.DAY_OF_WEEK);
        }

        @Fun
        private int getMaxDayOf(Calendar anyday) {
            int max = anyday.getActualMaximum(Calendar.DAY_OF_MONTH);
            return max;
        }

        private int getOffsetOfDay() {
            return getDayOfWeekOfFirstDayOf(mCalender) - 1
//                    + WEEK_TITLE_RES.length
                    ;
        }

        @Override
        public long getItemId(int position) {
            return position - getOffsetOfDay() + 1;
        }

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
                int num = position - getOffsetOfDay() + 1;
//                holder.recoverViewHeight();
                holder.view.setVisibility(View.VISIBLE);
                holder.view.setEnabled(true);
                holder.backgroundImageView.setVisibility(View.INVISIBLE);
                holder.foregroundImageView.setVisibility(View.VISIBLE);
                holder.foregroundTextView.setVisibility(View.VISIBLE);
                holder.foregroundTextView.setText(String.valueOf(num));

                fillBackground(holder.backgroundImageView, num);
            }
            return holder.view;
        }

        @Background
        void fillBackground(ImageView imageView, int day) {
//            return;
            Calendar today = ((Calendar) (mCalender.clone()));
            today.set(Calendar.DAY_OF_MONTH, day);
            String time = new SimpleDateFormat(Config.TIME_FORMAT).format(today.getTime());
            try {
                List<Moment> result = OpenHelperManager.getHelper(mContext, MomentDatabaseHelper.class).getDao(Moment.class)
                        .queryBuilder().where()
                        .eq("time", time).query();
                if (result.size() > 0) {
                    LogUtil.d(TAG, "fill background start,day: " + day + "; moment size: " + result.size());
                    String thumbPath = result.get(0).getThumbPath();
                    LogUtil.i(TAG, "thumb path: " + thumbPath);
                    if (thumbPath == null) CameraHelper.createThumbImage(mContext, result.get(0).getPath());
                    assert thumbPath != null;
                    Picasso.with(mContext).load(new File(thumbPath)).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setTag(result.get(0));//TODO add onclick
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }

        class CellView {
            final View view;
            ImageView backgroundImageView;
            ImageView foregroundImageView;
            TextView foregroundTextView;

            public CellView(ViewGroup parent) {
                this.view = LayoutInflater.from(mContext).inflate(R.layout.calendar_cell, parent, false);
                backgroundImageView = (ImageView) view.findViewById(R.id.backgroundImageView);
                foregroundImageView = (ImageView) view.findViewById(R.id.foregroundImageView);
                foregroundTextView = (TextView) view.findViewById(R.id.foregroundTextView);
                view.setTag(this);
            }

            public void setViewHeightSlim() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, mContext.getResources().getDisplayMetrics());
                view.setLayoutParams(params);
            }

            public void recoverViewHeight() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, mContext.getResources().getDisplayMetrics());
                view.setLayoutParams(params);
            }
        }
    }
}
