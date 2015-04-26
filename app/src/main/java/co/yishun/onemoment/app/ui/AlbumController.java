package co.yishun.onemoment.app.ui;

import java.util.Calendar;

public interface AlbumController {
    void showTodayMonthCalendar();

    void showNextMonthCalendar();

    void showPreviousMonthCalendar();

    void setOnMonthChangeListener(OnMonthChangeListener listener);

    interface OnMonthChangeListener {
        void onMonthChange(Calendar calendar);
    }

    int getTodayIndex();

    void notifyUpdate();
}
