package me.toxz.squarethumbnailvideoview.library;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.Calendar;

/**
 * Created by yyz on 6/9/15.
 */
public final class SquareThumbnailVideoView extends FrameLayout implements View.OnClickListener, MediaPlayer.OnCompletionListener, View.OnTouchListener {
    private static final String TAG = "SquareThumbnailVideoV";
    private static final int MAX_CLICK_DURATION = 200;
    private VideoView mVideoView;
    private ImageView mThumbImageView;
    private ImageButton mControlButton;
    private Bitmap mThumbnailBitmap = null;
    private Drawable mPlayButtonDrawable = getResources().getDrawable(R.drawable.stvv_defalut_play_button);
    private VideoAdapter mAdapter;
    private int currentVideoIndex = -1;
    private long startClickTime;
    private boolean isPause = false;

    public SquareThumbnailVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SquareThumbnailVideoView(Context context) {
        super(context);
        init(null, 0);
    }

    public SquareThumbnailVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareThumbnailVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(
                getMeasuredWidth(),
                getMeasuredHeight()
        );

        Log.i(TAG, "size: " + size);
        setMeasuredDimension(size, size);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.stvv_SquareThumbnailVideoView, defStyleAttr, 0);

        if (a.hasValue(R.styleable.stvv_SquareThumbnailVideoView_stvv_playButtonImage)) {
            mPlayButtonDrawable = a.getDrawable(R.styleable.stvv_SquareThumbnailVideoView_stvv_playButtonImage);
            assert mPlayButtonDrawable != null;
            mPlayButtonDrawable.setCallback(this);
        }
        a.recycle();

        initView();
    }

    private void initView() {
        mVideoView = new VideoView(getContext());
        mThumbImageView = new ImageView(getContext());
        mControlButton = new ImageButton(getContext());

        this.addView(mVideoView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mThumbImageView.setScaleType(ImageView.ScaleType.FIT_START);
        if (mThumbnailBitmap != null) {
            // mean pending set bitmap
            Log.d("initView", "do pended bitmap");
            setThumbnailBitmap(mThumbnailBitmap);
            mThumbImageView.setVisibility(VISIBLE);
        }

        this.addView(mThumbImageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mControlButton.setImageDrawable(mPlayButtonDrawable);
        mControlButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT > 16) mControlButton.setBackground(null);
        else mControlButton.setBackgroundResource(0);

        LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        buttonParams.gravity = Gravity.CENTER;
        this.addView(mControlButton, buttonParams);

        mControlButton.setOnClickListener(this);
    }

    /**
     * set thumbnail bitmap
     */
    public void setThumbnailBitmap(Bitmap bitmap) {
        // if called before view created, store it and set later
        if (mThumbImageView == null) {
            mThumbnailBitmap = bitmap;
            Log.d("setThumbnailBitmap", "view not created, pending set");
        } else {
            // view created, set it now
            mThumbnailBitmap = bitmap;
            Log.d("setThumbnailBitmap", "view created, set.");
            if (mAdapter == null || !mAdapter.setThumbnailImage(mThumbImageView, bitmap)) {
                // if set not adapter or setThumbnailImage return false
                Log.d("setThumbnailBitmap", "not override targeted method.");
                mThumbImageView.setImageBitmap(bitmap);
                mThumbImageView.setVisibility(VISIBLE);
            }

        }
    }

    // Delegate VideoView
    public void setVideoURI(Uri uri) {
        mVideoView.setVideoURI(uri);
    }

    public void seekTo(int msec) {
        mVideoView.seekTo(msec);
    }

    public void stopPlayback() {
        mVideoView.stopPlayback();
    }

    public void setMediaController(MediaController controller) {
        mVideoView.setMediaController(controller);
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mVideoView.setOnPreparedListener(l);
    }

    public boolean canSeekForward() {
        return mVideoView.canSeekForward();
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        mVideoView.setOnErrorListener(l);
    }

    public int getDuration() {
        return mVideoView.getDuration();
    }

    /**
     * Play single Video.
     *
     * @param path
     */
    public void setVideoPath(final String path) {
        mAdapter = new VideoAdapter<String>() {

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public String getItem(int position) {
                return path;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public String getVideoPath(int position) {
                return path;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean setThumbnailImage(ImageView thumbnailImageView, Bitmap bitmap) {
                return false;
            }
        };
    }

    public void setVideoAdapter(VideoAdapter adapter) {
        mAdapter = adapter;
        // set image again
        setThumbnailBitmap(mThumbnailBitmap);
    }

    public void resume() {
        mVideoView.resume();
    }

    public void suspend() {
        mVideoView.suspend();
    }

    public int getBufferPercentage() {
        return mVideoView.getBufferPercentage();
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        mVideoView.setOnCompletionListener(l);
    }

    public void pause() {
        mVideoView.pause();
    }

    public SurfaceHolder getHolder() {
        return mVideoView.getHolder();
    }

    public boolean canPause() {
        return mVideoView.canPause();
    }

    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        mVideoView.setZOrderMediaOverlay(isMediaOverlay);
    }

    public void setZOrderOnTop(boolean onTop) {
        mVideoView.setZOrderOnTop(onTop);
    }
    // Delegate VideoView

    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    public boolean canSeekBackward() {
        return mVideoView.canSeekBackward();
    }

    public boolean isPlaying() {
        return mVideoView.isPlaying();
    }

    @Override
    public void onClick(View v) {
        this.start();
    }

    /**
     * play the video or video in adapter.
     */
    private void start() {
        if (mAdapter == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.stvv_no_video_toast), Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 17) {
            mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @SuppressLint("NewApi")
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        mThumbImageView.setVisibility(View.GONE);
                    }
                    mVideoView.setOnInfoListener(null);
                    return true;
                }
            });
        } else {
            mThumbImageView.setVisibility(View.GONE);
        }
        mControlButton.setVisibility(INVISIBLE);
        mVideoView.setOnTouchListener(this);
        playVideo();
    }

    private void playVideo() {
        if (isPause) {
            mVideoView.start();// resume
            isPause = false;
        } else if (currentVideoIndex < mAdapter.getCount() - 1) {
            currentVideoIndex++;
            String path = mAdapter.getVideoPath(currentVideoIndex);
            mVideoView.setVideoPath(path);
            mVideoView.setOnCompletionListener(this);
            mVideoView.start();
        } else {
            currentVideoIndex = -1;
            if (mThumbImageView.getDrawable() != null) {
                mThumbImageView.setVisibility(VISIBLE);
            }
            mControlButton.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playVideo();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startClickTime = Calendar.getInstance().getTimeInMillis();
                return true;
            case MotionEvent.ACTION_UP:
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                // if near end, async pause will actually pause video when play end, and will cause resume fail
                boolean notNearingEnd = (mVideoView.getDuration() == -1 // only media may have no duration
                        && mVideoView.getCurrentPosition() < 800) || (mVideoView.getCurrentPosition() + 400 < mVideoView.getDuration());
                if (clickDuration < MAX_CLICK_DURATION && mVideoView.isPlaying() && notNearingEnd) {
                    mVideoView.setOnTouchListener(null);
                    mVideoView.pause();
                    isPause = true;
                    mControlButton.setVisibility(View.VISIBLE);
                    return true;
                }
                break;
        }
        return false;
    }
}
