//package co.yishun.onemoment.app.ui;
//
//import android.app.Fragment;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.*;
//import android.hardware.Camera;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.net.Uri;
//import android.os.*;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.*;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import co.yishun.onemoment.app.*;
//import co.yishun.onemoment.app.MyFFmpegFrameRecorder;
//import com.googlecode.javacv.FrameRecorder;
//import com.googlecode.javacv.cpp.opencv_core;
//import org.androidannotations.annotations.*;
//
//import java.io.*;
//import java.nio.Buffer;
//import java.nio.ShortBuffer;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
//
///**
// * This Fragment is used to record video.
// * <p/>
// * Created by Carlos on 2/3/15.
// */
//@EFragment(R.layout.fragment_record)
//public class RecordFragment extends Fragment {
//
//    //neon optimized openCV
//    static {
//        System.loadLibrary("checkneon");
//    }
//
//    public native static int checkNeonFromJNI();
//
//    private final static String CLASS_LABEL = "RecordFragment";
//
//    // default camera(the rear camera)
//    int defaultCameraId = -1;
//    int defaultScreenResolution = -1;
//
//    //preset width and height of he preview
//    int previewWidth = 480;
//    int screenWidth = 480;
//    int previewHeight = 480;
//    int screenHeight = 800;
//
//    // the selected camera
//    int cameraSelection = 0;
//    boolean isRecordingStarted = false;
//    private boolean isPreviewOn = false;
//    boolean isFlashOn = false;
//    /**
//     * {@link #setCamera()}
//     */
//    private boolean initSuccess = false;
//
//    long startTime = 0;
//    long recordTime = 1200;
//    boolean recordFinish = false;
//
//
//    @App
//    MyApplication iApplication;
//    //need release
//    private PowerManager.WakeLock mWakeLock;
//    @SystemService
//    PowerManager powerManager;
//
//    //Tool to record video
//    private volatile FrameRecorder videoRecorder;
//    private CameraView cameraView;
//
//    private AudioRecord audioRecord;
//    private AudioRecordRunnable audioRecordRunnable;
//    private Thread audioThread;
//    volatile boolean runAudioThread = true;
//    Camera.Parameters cameraParameters = null;
//
//
//    //TODO temp path of the video stored
//    private String strVideoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "rec_video.mp4";
//    private File fileVideoPath = null;
//    private Uri uriVideoPath = null;
//
//    //store byte[], height, width, depth, channel of the image from camera
//    private opencv_core.IplImage yuvIplImage = null;
//    //lock
//    private final int[] mVideoRecordLock = new int[0];
//    private final int[] mAudioRecordLock = new int[0];
//
//    private volatile long mAudioTimestamp = 0L;
//    private long mLastAudioTimestamp = 0L;
//    private volatile long mAudioTimeRecorded;
//
//    private long frameTime = 0L;
//    private SavedFrames lastSavedframe = new SavedFrames(null, 0L);
//    private long mVideoTimestamp = 0L;
//    private boolean isRecordingSaved = false;
//    private boolean isFinalizing = false;
//
//    //store first frame to preview video
//    private String lastImagePath = null;
//    //store last to display on screen after record end
//    private String firstImagePath = null;
//    private byte[] firstData = null;
//
//    private Camera cameraDevice;
//
//    @ViewById
//    ImageView recordSurfaceImage;
//
//    @ViewById
//    ImageButton recordVideoBtn;
//    @ViewById
//    ViewGroup recordSurfaceParent;
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mWakeLock == null) {
//            acquireWakeLock();
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (!isFinalizing)
//            getActivity().finish();
//        releaseWakeLock();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        runAudioThread = false;
//        releaseResources();
//
//    }
//
//    @AfterInject
//    void acquireWakeLock() {
//        if (mWakeLock == null) {
//            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
//            mWakeLock.acquire();
//        }
//
//    }
//
//    void releaseWakeLock() {
//        if (mWakeLock != null) {
//            mWakeLock.release();
//            mWakeLock = null;
//        }
//    }
//
//
//    @AfterViews
//    public void initLayout() {
//        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
//            //TODO handle the front camera
//        }
//
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        //TODO may produce NullPointerException
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        //Find screen dimensions
//        screenWidth = displaymetrics.widthPixels;
//        screenHeight = displaymetrics.heightPixels;
//
//        initCamera();
//    }
//
//    @Background
//    void initCamera() {
//        boolean result = setCamera();
//
//        if (!initSuccess) {
//
//            initVideoRecorder();
//            startRecording();
//
//            initSuccess = true;
//        }
//
//        onCameraInit();
//    }
//
//
//    @UiThread
//    void onCameraInit() {
//        if (!handleSurfaceChanged()) return;
//
//
//        if (recordSurfaceParent.getChildCount() > 0)
//            recordSurfaceParent.removeAllViews();
//
//        cameraView = new CameraView(this.getActivity(), cameraDevice);
//
//        //set surface's width and height
//        RelativeLayout.LayoutParams layoutParam1 = new RelativeLayout.LayoutParams(screenWidth, (int) (screenWidth * (previewWidth / (previewHeight * 1f))));
//        layoutParam1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//        //int margin = Util.calculateMargin(previewWidth, screenWidth);
//        //layoutParam1.setMargins(0,margin,0,margin);
//        RelativeLayout.LayoutParams layoutParam2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        layoutParam2.topMargin = screenWidth;
//
//
//        View view = new View(getActivity());
//        view.setFocusable(false);
//        view.setBackgroundColor(Color.BLACK);
//        view.setFocusableInTouchMode(false);
//
//        recordSurfaceParent.addView(cameraView, layoutParam1);
//        recordSurfaceParent.addView(view, layoutParam2);
//    }
//
//    private boolean handleSurfaceChanged() {
//        if (cameraDevice == null) {
//            Log.e("recorder", "get camera failed");
//            return false;
//        }
//        //获取摄像头的所有支持的分辨率
//        List<Camera.Size> resolutionList = cameraDevice.getParameters().getSupportedPreviewSizes();
//        if (resolutionList != null && resolutionList.size() > 0) {
//            Collections.sort(resolutionList, new Comparator<Camera.Size>() {
//                        @Override
//                        public int compare(Camera.Size size1, Camera.Size size2) {
//                            if (size1.height != size2.height)
//                                return size1.height - size2.height;
//                            else
//                                return size1.width - size2.width;
//                        }
//                    }
//            );
//            Camera.Size previewSize = null;
//            if (defaultScreenResolution == -1) {
//                boolean hasSize = false;
//                //如果摄像头支持480*480，那么强制设为640*480
//                for (Camera.Size size : resolutionList) {
//                    if (size != null && size.width == 480 && size.height == 480) {
//                        previewSize = size;
//                        hasSize = true;
//                        break;
//                    }
//                }
//                //如果不支持设为中间的那个
//                if (!hasSize) {
//                    int mediumResolution = resolutionList.size() / 2;
//                    if (mediumResolution >= resolutionList.size())
//                        mediumResolution = resolutionList.size() - 1;
//                    previewSize = resolutionList.get(mediumResolution);
//                }
//            } else {
//                if (defaultScreenResolution >= resolutionList.size())
//                    defaultScreenResolution = resolutionList.size() - 1;
//                previewSize = resolutionList.get(defaultScreenResolution);
//            }
//            //获取计算过的摄像头分辨率
//            if (previewSize != null) {
//                previewWidth = previewSize.width;
//                previewHeight = previewSize.height;
//                cameraParameters.setPreviewSize(previewWidth, previewHeight);
//                if (videoRecorder != null) {
//                    videoRecorder.setImageWidth(previewWidth);
//                    videoRecorder.setImageHeight(previewHeight);
//                }
//
//            }
//        }
//        cameraParameters.setPreviewFrameRate(15);
//        //构建一个IplImage对象，用于录制视频
//        //和opencv中的cvCreateImage方法一样
//        yuvIplImage = opencv_core.IplImage.create(previewHeight, previewWidth, IPL_DEPTH_8U, 2);
//
//        //系统版本为8一下的不支持这种对焦
//        //Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO
//        //TODO
//        cameraDevice.setDisplayOrientation(180);
//        List<String> focusModes = cameraParameters.getSupportedFocusModes();
//        if (focusModes != null) {
//            Log.i("video", Build.MODEL);
//            if (((Build.MODEL.startsWith("GT-I950")) || (Build.MODEL.endsWith("SCH-I959")) || (Build.MODEL.endsWith("MEIZU MX3"))) && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
//                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//            } else
//                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
//        }
//        cameraDevice.setParameters(cameraParameters);
//        return true;
//    }
//
//    RecorderParameters recorderParameters;
//
//    @SupposeBackground
//    private void initVideoRecorder() {
//        strVideoPath = Util.createFinalPath(getActivity());//Util.createTempPath(tempFolderPath);
//
//        recorderParameters = new RecorderParameters();
//
//
//        fileVideoPath = new File(strVideoPath);
//        videoRecorder = new MyFFMPEGFrameRecorder(strVideoPath);
//
//
//        audioRecordRunnable = new AudioRecordRunnable();
//        audioThread = new Thread(audioRecordRunnable);
//    }
//
//    class AudioRecordRunnable implements Runnable {
//
//        int bufferSize;
//        short[] audioData;
//        int bufferReadResult;
//        private final AudioRecord audioRecord;
//        public volatile boolean isInitialized;
//        private int mCount = 0;
//
//        private AudioRecordRunnable() {
//            bufferSize = AudioRecord.getMinBufferSize(recorderParameters.audioSamplingRate,
//                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, recorderParameters.audioSamplingRate,
//                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
//            audioData = new short[bufferSize];
//        }
//
//        /**
//         * shortBuffer包含了音频的数据和起始位置
//         *
//         * @param shortBuffer
//         */
//        private void record(ShortBuffer shortBuffer) {
//            try {
//                synchronized (mAudioRecordLock) {
//                    if (videoRecorder != null) {
//                        this.mCount += shortBuffer.limit();
//                        videoRecorder.record(0, new Buffer[]{shortBuffer});
//                    }
//                    return;
//                }
//            } catch (FrameRecorder.Exception localException) {
//            }
//        }
//
//        /**
//         * 更新音频的时间戳
//         */
//        private void updateTimestamp() {
//            if (videoRecorder != null) {
//                int i = (int) (this.mCount / 0.0441D);
//                if (mAudioTimestamp != i) {
//                    mAudioTimestamp = i;
//                    mAudioTimeRecorded = System.nanoTime();
//                }
//            }
//        }
//
//        public void run() {
//            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//            this.isInitialized = false;
//            if (audioRecord != null) {
//                //判断音频录制是否被初始化
//                while (this.audioRecord.getState() == 0) {
//                    try {
//                        Thread.sleep(100L);
//                    } catch (InterruptedException localInterruptedException) {
//                    }
//                }
//                this.isInitialized = true;
//                this.audioRecord.startRecording();
//                while (((runAudioThread) || (mVideoTimestamp > mAudioTimestamp)) && (mAudioTimestamp < (1000 * recordingTime))) {
//                    updateTimestamp();
//                    bufferReadResult = this.audioRecord.read(audioData, 0, audioData.length);
//                    if ((bufferReadResult > 0) && ((recording && rec) || (mVideoTimestamp > mAudioTimestamp)))
//                        record(ShortBuffer.wrap(audioData, 0, bufferReadResult));
//                }
//                this.audioRecord.stop();
//                this.audioRecord.release();
//            }
//        }
//    }
//
//    void startRecording() {
//        try {
//            videoRecorder.start();
//            audioThread.start();
//        } catch (FrameRecorder.Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    void stopRecording() {
//        isFinalizing = true;
//        recordFinish = true;
//        runAudioThread = false;
//        handleRecordData();
//    }
//
//    //TODO add last data
//    @Background
//    void handleRecordData() {
//        if (firstData != null)
//            getFirstCapture(firstData);
//        isFinalizing = false;
//        if (videoRecorder != null) {
//            releaseResources();
//        }
//    }
//
//    @UiThread
//    void onHandlerFinished() {
//        registerVideo();
//        //TODO notify finish and prepare for another shooting.
//        videoRecorder = null;
//    }
//
//    /**
//     * 依据byte[]里的数据合成一张bitmap，
//     * 截成480*480，并且旋转90度后，保存到文件
//     *
//     * @param data
//     */
//    @SupposeBackground
//    void getFirstCapture(byte[] data) {
//
//        publishProgress(10);
//
//        String captureBitmapPath = CONSTANTS.CAMERA_FOLDER_PATH;
//
//        captureBitmapPath = Util.createImagePath(FFmpegRecorderActivity.this);
//        YuvImage localYuvImage = new YuvImage(data, 17, previewWidth, previewHeight, null);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        FileOutputStream outStream = null;
//
//        publishProgress(50);
//
//        try {
//            File file = new File(captureBitmapPath);
//            if (!file.exists())
//                file.createNewFile();
//            localYuvImage.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight), 100, bos);
//            Bitmap localBitmap1 = BitmapFactory.decodeByteArray(bos.toByteArray(),
//                    0, bos.toByteArray().length);
//
//            bos.close();
//
//            Matrix localMatrix = new Matrix();
//            if (cameraSelection == 0)
//                localMatrix.setRotate(90.0F);
//            else
//                localMatrix.setRotate(270.0F);
//
//            Bitmap localBitmap2 = Bitmap.createBitmap(localBitmap1, 0, 0,
//                    localBitmap1.getHeight(),
//                    localBitmap1.getHeight(),
//                    localMatrix, true);
//
//            publishProgress(70);
//
//            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
//            localBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, bos2);
//
//            outStream = new FileOutputStream(captureBitmapPath);
//            outStream.write(bos2.toByteArray());
//            outStream.close();
//
//            localBitmap1.recycle();
//            localBitmap2.recycle();
//
//            publishProgress(90);
//
//            isFirstFrame = false;
//            imagePath = captureBitmapPath;
//        } catch (FileNotFoundException e) {
//            isFirstFrame = true;
//            e.printStackTrace();
//        } catch (IOException e) {
//            isFirstFrame = true;
//            e.printStackTrace();
//        }
//    }
//
//
//    @Click(R.id.recordVideoBtn)
//    public void onClick() {
//
//    }
//
//    /**
//     * register video in the Context Provider.
//     */
//    private void registerVideo() {
//        //TODO need?
//    }
//
//    /**
//     * to save video file in the background.
//     */
//    @Background
//    private void saveRecording() {
//
//    }
//
//    /**
//     * release all the large resources when OnDestroy() or end recording.
//     */
//    private void releaseResources() {
//        //component life may be more long after onDestroy(), need to release explicitly
//        if (cameraView != null) {
//            cameraView.release();
//            cameraView = null;
//        }
//        firstData = null;
//    }
//
//
//    /**
//     * call when press record btn. To set all param the origin value.
//     */
//    private void initRecording() {
//
//    }
//
//    /**
//     * set the {@link #cameraDevice} field.
//     *
//     * @return false if failed.
//     */
//    private boolean setCamera() {
//        try {
//
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
//                int numberOfCameras = Camera.getNumberOfCameras();
//
//                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//                for (int i = 0; i < numberOfCameras; i++) {
//                    Camera.getCameraInfo(i, cameraInfo);
//                    if (cameraInfo.facing == cameraSelection) {
//                        defaultCameraId = i;
//                    }
//                }
//            }
//            stopPreview();
//            if (mCamera != null)
//                mCamera.release();
//
//            if (defaultCameraId >= 0)
//                cameraDevice = Camera.open(defaultCameraId);
//            else
//                cameraDevice = Camera.open();
//
//        } catch (Exception e) {
//            return false;
//        }
//        return true;
//    }
//
//
//    private void onSurfaceChanged() {
//
//    }
//
//    class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
//        private Camera mCamera;
//        private Camera.Parameters mCameraParas;
//
//        public void release() {
//            this.stopPreview();
//            mCamera.setPreviewCallback(null);
//            mCamera.release();
//            mCamera = null;
//        }
//
//        public CameraView(Context context, Camera camera) {
//            super(context);
//            mCamera = camera;
//            mCameraParas = mCamera.getParameters();
//            getHolder().addCallback(CameraView.this);
//            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//            mCamera.setPreviewCallback(CameraView.this);
//        }
//
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            try {
//                stopPreview();
//                mCamera.setPreviewDisplay(holder);
//            } catch (IOException exception) {
//                mCamera.release();
//                mCamera = null;
//            }
//        }
//
//        @Override
//        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            if (isPreviewOn)
//                mCamera.stopPreview();
//            onSurfaceChanged();
//            startPreview();
//            mCamera.autoFocus(null);
//        }
//
//        @Override
//        public void surfaceDestroyed(SurfaceHolder holder) {
//
//        }
//
//        @Override
//        public void onPreviewFrame(byte[] data, Camera camera) {
//
//        }
//
//
//        public void startPreview() {
//            if (!isPreviewOn && mCamera != null) {
//                isPreviewOn = true;
//                mCamera.startPreview();
//            }
//        }
//
//        public void stopPreview() {
//            if (isPreviewOn && mCamera != null) {
//                isPreviewOn = false;
//                mCamera.stopPreview();
//            }
//        }
//    }
//
//}