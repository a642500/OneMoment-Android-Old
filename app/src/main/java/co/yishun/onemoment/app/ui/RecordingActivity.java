package co.yishun.onemoment.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.MyApplication;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.convert.Converter;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import org.androidannotations.annotations.*;

import java.io.*;
import java.sql.SQLException;

//TODO stop record by MediaRecorder.setMaxDuration()

/**
 * This activity uses the camera/camcorder as the A/V source for the {@link MediaRecorder} API.
 * A {@link TextureView} is used as the camera preview which limits the code to API 14+.
 * Created by Carlos on 2/14/15.
 */
//@Fullscreen
@EActivity(R.layout.recording_layout)
public class RecordingActivity extends Activity {
    public static final int NOT_PREPARED = 0;
    public static final int PREPARED = 1;
    public static final int PREPARED_FAILED = -1;
    private static final String TAG = LogUtil.makeTag(RecordingActivity.class);
    @ViewById(R.id.surfaceView)
    TextureView mPreview;
    private Camera mCamera;
    //    @ViewById(R.id.recordVideoBtn)
//    ImageButton captureButton;
    private MediaRecorder mMediaRecorder;
    private String mCurrentVideoPath = null;
    private boolean isRecording = false;
    private int prepareStatus = NOT_PREPARED;
    /*
    Dialog to display convert progress.
     */
    private AlertDialog mConvertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mConvertDialog = new MaterialDialog.Builder(this).content(getString(R.string.recordConvertingHint)).cancelable(false).progress(true, 0).build();

    }

    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases {@link MediaRecorder} and {@link Camera}. When not recording,
     * it prepares the {@link MediaRecorder} and starts recording.
     *
     * @param view the view generating the event.
     */
    @Fun
    @Click(R.id.recordVideoBtn)
    void onCaptureClick(View view) {
        if (!isRecording) {
            record();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Fun
    @Click
    void albumBtnClicked(View view) {
        //TODO
        new AlbumActivity_.IntentBuilder_(this).start();
    }

    @ViewById
    ImageSwitcher recordFlashSwitch;
    @ViewById
    ImageSwitcher cameraSwitch;

    @AfterViews
    void initViews() {
        //TODO can switch when recording?
        //TODO flashlight  http://stackoverflow.com/questions/6068803/how-to-turn-on-camera-flash-light-programmatically-in-android
        PackageManager packageManager = getPackageManager();

        boolean hasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        recordFlashSwitch.setEnabled(hasFlash);
        recordFlashSwitch.setVisibility(hasFlash ? View.VISIBLE : View.INVISIBLE);
        recordFlashSwitch.getCurrentView().setOnClickListener(v -> {
                    if (mCamera != null) {
                        Camera.Parameters p = mCamera.getParameters();
                        p.setFlashMode(
//                                isChecked ?
                                Camera.Parameters.FLASH_MODE_TORCH
//                                        : Camera.Parameters.FLASH_MODE_OFF
                        );
                        mCamera.setParameters(p);
                        recordFlashSwitch.showNext();
                    }
                }

        );
        recordFlashSwitch.getNextView().setOnClickListener(v -> {
                    if (mCamera != null) {
                        Camera.Parameters p = mCamera.getParameters();
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(p);
                        recordFlashSwitch.showNext();
                    }
                }

        );
//        recordFlashSwitch.setOnCheckedChangeListener(
//                hasFlash ?
//                        (buttonView, isChecked) -> {
//
//                        }
//                        : null
//        );

        boolean hasFront = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        cameraSwitch.setEnabled(hasFront);
        cameraSwitch.setVisibility(hasFront ? View.VISIBLE : View.INVISIBLE);
        cameraSwitch.getCurrentView().setOnClickListener(
                hasFront ?
                        v -> {
                            CameraHelper.setFrontCamera(!CameraHelper.isFrontCamera());
                            releaseCamera();
//                            recordFlashSwitch.reset();
                            preview();
                        }
                        : null
        );
        cameraSwitch.getNextView().setOnClickListener(
                hasFront ?
                        v -> {
                            CameraHelper.setFrontCamera(!CameraHelper.isFrontCamera());
                            releaseCamera();
//                            recordFlashSwitch.reset();
                            preview();
                        }
                        : null
        );

//        setOnCheckedChangeListener(
//                hasFront ?
//                        (v, isChecked) -> {
//                            CameraHelper.setFrontCamera(isChecked);
//                            releaseCamera();
////                            recordFlashSwitch.setChecked(false);//reset flashlight switch's status
//                            preview();
//                        }
//                        : null
//        );


    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        preview();
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause start: " + System.currentTimeMillis());
        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
        LogUtil.d(TAG, "onPause end: " + System.currentTimeMillis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    @Background
    void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            // Don't use:mCamera.release();
            CameraHelper.releaseCamera(mCamera);
            mCamera = null;
//            ((MyApplication) getApplication()).isRelease = true;
        }

    }


    //    @AfterViews
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Background
    void preview() {

        // BEGIN_INCLUDE (configure_preview)
//        while (!((MyApplication) getApplication()).isRelease) {
//                try {
//                    LogUtil.i(TAG, "camera not release, wait");
//                    wait(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//            }
//        }
        mCamera = CameraHelper.getCameraInstance();
        LogUtil.d(TAG, "unlock at getInstance");
        ((MyApplication) getApplication()).isRelease = false;
        final Camera.Parameters parameters = mCamera.getParameters();
        final Camera.Size optimalPreviewSize = CameraHelper.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), Config.getDefaultCameraSize().first, Config.getDefaultCameraSize().second);
        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);

        mPreview.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            mPreview.setOpaque(false);
            Matrix mat = calculatePreviewMatrix(mPreview, Config.getDefaultCameraSize(), optimalPreviewSize);
            mPreview.setTransform(mat);
        });

        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            LogUtil.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            prepareStatus = PREPARED_FAILED;
        }
//        mPreview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        mCamera.setOneShotPreviewCallback((data, camera) -> hideSplash());
        mCamera.startPreview();
    }


    @ViewById
    ImageView welcomeOverlay;

    @UiThread
    void hideSplash() {
        welcomeOverlay.setVisibility(View.GONE);
    }

    /**
     * Asynchronous task for preparing the {@link MediaRecorder} since it's a long blocking
     * operation.
     */
    @SupposeBackground
    void prepare() {
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size optimalVideoSize = CameraHelper.getOptimalPreviewSize(parameters.getSupportedVideoSizes(), 480, 480);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        profile.videoFrameWidth = optimalVideoSize.width;
        profile.videoFrameHeight = optimalVideoSize.height;
        profile.videoFrameRate = 30;
        profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
        profile.videoCodec = MediaRecorder.VideoEncoder.H264;
        profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;

        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mCurrentVideoPath = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO, this).toString();
        mMediaRecorder.setOutputFile(mCurrentVideoPath);
        mMediaRecorder.setOrientationHint(90);
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            LogUtil.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            prepareStatus = PREPARED_FAILED;
        } catch (IOException e) {
            LogUtil.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            prepareStatus = PREPARED_FAILED;
        }
        prepareStatus = PREPARED;
    }

    @Background
    void record() {
        prepare();
        if (prepareStatus == PREPARED) {
            mMediaRecorder.start();
            isRecording = true;
            stopRecordAfterSomeTime();
        } else {
            releaseMediaRecorder();
        }
    }

    @UiThread(delay = 1200L)
    void stopRecordAfterSomeTime() {
        LogUtil.i(TAG, "time run out, isRecording: " + isRecording);
        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            isRecording = false;
            releaseCamera();
            Toast.makeText(RecordingActivity.this, "ok!!", Toast.LENGTH_LONG).show();
            startConvert();
        }
    }

    @UiThread
    void startConvert() {
        //TODO alert convert
        mConvertDialog.show();
        convert(mCurrentVideoPath, CameraHelper.getConvertedMediaFile(mCurrentVideoPath));
    }

    @Background
    void convert(String from, String to) {
        Converter.with(this).from(from).cropToStandard().to(to).setHandler(new FFmpegExecuteResponseHandler() {
            @Override
            public void onSuccess(String message) {
                LogUtil.i(TAG, "success: " + message);
            }

            @Override
            public void onProgress(String message) {
                LogUtil.i(TAG, "progress: " + message);

            }

            @Override
            public void onFailure(String message) {
                LogUtil.e(TAG, "fail: " + message);

            }

            @Override
            public void onStart() {
                LogUtil.i(TAG, "start");

            }

            @Override
            public void onFinish() {
                LogUtil.i(TAG, "finish");
                onConvert(0, from, to);
            }
        }).start();
    }

    private String convertInputStreamToString(InputStream is) throws IOException {

     /*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }


    @UiThread
    void onConvert(int exitCode, String origin, String converted) {
        if (0 == exitCode) {
            Toast.makeText(this, "Convert success", Toast.LENGTH_LONG).show();
            saveData(origin, converted);
        } else Toast.makeText(this, "Convert Failed: ErrorCode " + exitCode, Toast.LENGTH_LONG).show();
        mConvertDialog.dismiss();
    }

    @Background
    void saveData(String origin, String converted) {
        //delete origin file
        File file = new File(origin);
        if (file.exists() && new File(converted).exists()) {
            file.delete();
        }

        try {
            //create and save thumb image
            String thumbPath = CameraHelper.createThumbImage(this, converted);

            //register at database
            Moment moment = new Moment.MomentBuilder().setPath(converted).setThumbPath(thumbPath).build();
            OpenHelperManager.getHelper(this, MomentDatabaseHelper.class).getDao(Moment.class).create(moment);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param previewView view to display preview
     * @param cropSize    targeted preview size. Null to use default size {@link co.yishun.onemoment.app.config.Config#}
     * @return matrix to apply {@link TextureView#setTransform(Matrix)}
     */
    @Fun
    private Matrix calculatePreviewMatrix(@NonNull final TextureView previewView, @NonNull final Pair<Integer, Integer> cropSize, @NonNull final Camera.Size size) {
        Matrix mat = new Matrix();

        int viewWidth = previewView.getWidth();
        int viewHeight = previewView.getHeight();
        LogUtil.v(TAG, "view width: " + viewWidth);
        if (viewHeight != viewWidth)
            LogUtil.w(TAG, "preview view width not equals height, width is " + viewWidth + ", height is" + viewHeight);
        LogUtil.v(TAG, "crop width: " + cropSize.first);
        if (!cropSize.first.equals(cropSize.second)) {
            LogUtil.w(TAG, "target size first not equals second, first is " + cropSize.first + ", second is" + cropSize.second);
        }

        //set scale
        float scaleX = viewWidth / cropSize.first;
        float scaleY = viewHeight / cropSize.second;
        LogUtil.i(TAG, "scaleX " + scaleX + "; scaleY " + scaleY);
        mat.setScale(scaleX, scaleY);

        //move to center
        mat.postTranslate(size.width / 2 - viewWidth / 2, size.height / 2 - viewHeight / 2);
        return mat;
    }

}
