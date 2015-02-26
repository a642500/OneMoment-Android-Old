package co.yishun.onemoment.app.recorder;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import co.yishun.onemoment.app.util.LogUtil;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.UiThread;

import java.io.IOException;

/**
 * This class is to handle the android Camera api and openCV functions.
 * <p/>
 * Don't inject to get instance! Use static factory methods to get instance.
 * <p/>
 * Created by kotlin on 2/4/15.
 */
@EBean
public class CameraRecorder implements Recorder {

    public static final String TAG = "CameraRecorder";

    private Context mContext;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder = new MediaRecorder();
    private RecordWriter mRecordWriter;
    private RecorderParameters mRecorderParameters;
    private boolean isParamsSet = false;
    private Recorder.OnRecordStatusListener mListener = null;
    //TODO setListener

    //Don't use it.
    CameraRecorder(Context context) {
        mContext = context;
    }

    public static Recorder getDefaultRecorder(Context context) {
        Recorder recorder = null;
        try {
            recorder = new CameraRecorder(context).setWriter(new SimpleWriter()).setParams(new RecorderParameters());
        } catch (IllegalStateRecorderException e) {
            e.printStackTrace();
        }
        assert recorder != null;
        return recorder;
    }

    @Override
    public void setOnRecordStatusListener(OnRecordStatusListener listener) {
        mListener = listener;
    }

    @Override
    public SurfaceView startPreview() {
        return new CameraView(mContext);//preview start when this CameraView's surfaceCreate() is called.
    }

    @Override
    public void stopPreview() {
        //TODO delete
//        mCamera.stopPreview();
    }

    /**
     * connect to camera
     *
     * @throws CameraInitRecorderException   when fail connecting to camera.
     * @throws IllegalStateRecorderException when try connect again with camera connected.
     */
    @SupposeBackground
    public void init() throws CameraInitRecorderException, IllegalStateRecorderException {
        if (!checkCameraHardware(mContext)) throw new CameraInitRecorderException("Not support camera");
        if (mCamera != null) throw new IllegalStateRecorderException("Recorder has been initialized.");
        try {
            mCamera = Camera.open();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setVideoFrameRate(30);
            //TODO store path
            mMediaRecorder.setOutputFile(mRecordWriter.getOutputPath());
//        mCamera.setPreviewDisplay();http://developer.android.com/guide/topics/media/camera.html#access-camera
            isParamsSet = true;
            mMediaRecorder.prepare();

        } catch (IllegalStateException e) {
            LogUtil.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        } catch (IOException e) {
            LogUtil.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            throw new CameraInitRecorderException("open camera failed");
        }
    }

    @Override
    public void startRecord() throws IllegalStateRecorderException {
        mMediaRecorder.start();
    }

    @Override
    public void stopRecord() throws IllegalStateRecorderException {
        mMediaRecorder.stop();
        releaseMediaRecorder();//reset
        onRecordFinish(true);
    }

    @Override
    public Recorder setParams(RecorderParameters parameters) throws IllegalStateRecorderException {
        if (isParamsSet) {
            throw new IllegalStateRecorderException("parameters has been set, cannot be changed");
        } else {
            mRecorderParameters = parameters;
            isParamsSet = true;
        }
        return this;
    }

    @Override
    public RecorderParameters getParams() {
        return mRecorderParameters;
    }


    @Override
    public RecordWriter getWriter() {
        return mRecordWriter;
    }

    @Override
    public Recorder setWriter(RecordWriter writer) {
        mRecordWriter = writer;
        return this;
    }


    @Override
    public void release() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * @return if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @UiThread
    public void onRecordFinish(boolean success) {
        if (mListener != null) {
            mListener.onRecordFinish(success);
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    @UiThread
    public void onSaveFinish(boolean success) {
        if (mListener != null) {
            mListener.onSaveFinish(success);
            //TODO remove save finish callback ,because saving is alone with recording.
        }
    }


    private class CameraView extends SurfaceView implements SurfaceHolder.Callback {
        private final SurfaceHolder mHolder;

        public CameraView(Context context) {
            super(context);
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            //setPreviewOrientation
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.startPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.startPreview();
            } catch (Exception e) {
                LogUtil.d("CameraView", "Error starting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

    }
}
