package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.ui.PlayActivity_;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Carlos on 2015/4/4.
 */
@EBean
public class CalendarAdapter extends BaseAdapter {
    public static final int[] WEEK_TITLE_RES = new int[]{
//                R.string.albumWeekTitleSun,
//                R.string.albumWeekTitleMon,
//                R.string.albumWeekTitleTue,
//                R.string.albumWeekTitleWed,
//                R.string.albumWeekTitleThu,
//                R.string.albumWeekTitleFri,
//                R.string.albumWeekTitleSat
    };
    private static final String TAG = LogUtil.makeTag(CalendarAdapter.class);
    private final Calendar mCalender;
    private final Context mContext;


    public CalendarAdapter(Context context) {
        this.mContext = context;
        mCalender = Calendar.getInstance();
    }

    public void setCalender(Calendar calendar) {
        mCalender.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        mCalender.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        notifyDataSetInvalidated();
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
            // not use because new implement WEEK_TITLE_RES.length == 0
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
            holder.foregroundTextView.setText(String.valueOf(num));

            fillBackground(holder, num);
        }
        return holder.view;
    }

    @Background void fillBackground(CellView holder, int day) {
//            return;
        Calendar thisMonth = ((Calendar) (mCalender.clone()));
        thisMonth.set(Calendar.DAY_OF_MONTH, day);
        String time = new SimpleDateFormat(Config.TIME_FORMAT).format(thisMonth.getTime());
        try {
            List<Moment> result = null;
            Dao<Moment, Integer> dao = OpenHelperManager.getHelper(mContext, MomentDatabaseHelper.class).getDao(Moment.class);

            if (AccountHelper.isLogin(mContext)) {
                final Where<Moment, Integer> w = dao.queryBuilder().where();
                result = w.and(
                        w.eq("time", time),
                        w.or(
                                w.eq("owner", AccountHelper.getIdentityInfo(mContext).get_id()),
                                w.eq("owner", "LOC"))
                ).query();
            } else {
                result = dao.queryBuilder().where()
                        .eq("time", time).and().eq("owner", "LOC").query();
            }
            Calendar today = Calendar.getInstance();
            if (result != null && result.size() > 0) {
                LogUtil.d(TAG, "fill background start,day: " + day + "; moment size: " + result.size());
                String thumbPath = result.get(0).getThumbPath();
                LogUtil.i(TAG, "thumb path: " + thumbPath);
                if (thumbPath == null) CameraHelper.createThumbImage(mContext, result.get(0).getPath());
                assert thumbPath != null;
                Picasso.with(mContext).load(new File(thumbPath)).into(holder.backgroundImageView);
                holder.backgroundImageView.setVisibility(View.VISIBLE);
                holder.backgroundImageView.setTag(result.get(0));

                // set orange if it is today
                if (sameDay(thisMonth, today))
                    holder.foregroundImageView.setImageResource(R.drawable.bg_calendar_cell_orango);
                else {
                    holder.foregroundImageView.setVisibility(View.INVISIBLE);
                }

                holder.view.setOnClickListener(v -> {
                    Moment moment = (Moment) ((CellView) v.getTag()).backgroundImageView.getTag();
                    if (moment != null) {
                        PlayActivity_.intent(v.getContext()).extra("moment", moment).start();
                    }
                });
            } else {
                holder.foregroundImageView.setVisibility(View.VISIBLE);
                holder.backgroundImageView.setVisibility(View.INVISIBLE);
                // set orange if it is today
                if (sameDay(thisMonth, today))
                    holder.foregroundImageView.setImageResource(R.drawable.bg_calendar_cell_orango_empty);
                else if (before(thisMonth, today)) {
                    holder.foregroundImageView.setImageResource(R.drawable.bg_calendar_cell_grey_disable);
                } else {
                    holder.foregroundImageView.setImageResource(R.drawable.bg_calendar_cell_grey);
                }
            }


        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean sameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean before(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR)
                || (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) < c2.get(Calendar.MONTH))
                || (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH) < c2.get(Calendar.DAY_OF_MONTH));
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