package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import co.yishun.onemoment.app.util.LogUtil;

/**
 * Created by Carlos on 3/15/15.
 */
public class SquareFrameLayout extends FrameLayout {
    private static final String TAG = LogUtil.makeTag(SquareFrameLayout.class);

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLayout(Context context) {
        super(context);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(
                getMeasuredWidth(),
                getMeasuredHeight()
        );

        LogUtil.i(TAG, "size: " + size);
        setMeasuredDimension(size, size);
    }
}
