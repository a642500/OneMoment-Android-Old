package co.yishun.onemoment.app.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Switch;
import co.yishun.onemoment.app.R;

/**
 * TODO: document your custom view class.
 */
public class PictureSwitchView extends Switch {

    private Drawable mExampleDrawable;

    
    public PictureSwitchView(Context context) {
        super(context);
        init(null, 0);
    }
    
    public PictureSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }
    
    public PictureSwitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    
    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PictureSwitchView, defStyle, 0);
        

        if (a.hasValue(R.styleable.PictureSwitchView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.PictureSwitchView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }
        
        a.recycle();
        
        // Set up a default TextPaint object

    }
    

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        
        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;
        

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }
    
    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
