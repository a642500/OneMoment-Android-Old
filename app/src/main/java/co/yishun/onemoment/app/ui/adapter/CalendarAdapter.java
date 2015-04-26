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
import co.yishun.onemoment.app.ui.AlbumController;
import co.yishun.onemoment.app.ui.PlayActivity_;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;
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
@Deprecated
public class CalendarAdapter extends BaseAdapter implements AlbumController {
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
    private OnMonthChangeListener onMonthChangeListener;


    public CalendarAdapter(Context context) {
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

    @Override
    public int getTodayIndex() {
        return 0;
    }

    @Override public void notifyUpdate() {
        LogUtil.e(TAG, "not support method");
    }

    private void onChange(Calendar calendar) {
        if (onMonthChangeListener != null) {
            onMonthChangeListener.onMonthChange(calendar);
        }
    }

    public void showPreviousMonthCalendar() {
        mCalender.add(Calendar.MONTH, -1);
        notifyDataSetInvalidated();
    }

    public void showNextMonthCalendar() {
        mCalender.add(Calendar.MONTH, 1);
        notifyDataSetInvalidated();
    }

    public void showTodayMonthCalendar() {
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

            Calendar today = Calendar.getInstance();
            if (mCalender.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && mCalender.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) == num)
                holder.foregroundImageView.setImageResource(R.drawable.bg_calender_cell_orango);
            else holder.foregroundImageView.setImageResource(R.drawable.bg_calender_cell_grey);

            fillBackground(holder, num);
        }
        return holder.view;
    }

    @Background void fillBackground(CellView holder, int day) {
//            return;
        ImageView imageView = holder.backgroundImageView;
        Calendar todayOfMonth = ((Calendar) (mCalender.clone()));
        todayOfMonth.set(Calendar.DAY_OF_MONTH, day);
        String time = new SimpleDateFormat(Config.TIME_FORMAT).format(todayOfMonth.getTime());
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
                imageView.setTag(result.get(0));

                holder.view.setOnClickListener(v -> {
                    Moment moment = (Moment) ((CellView) v.getTag()).backgroundImageView.getTag();
                    if (moment != null) {
                        PlayActivity_.intent(v.getContext()).extra("moment", moment).start();
                    }
                });
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