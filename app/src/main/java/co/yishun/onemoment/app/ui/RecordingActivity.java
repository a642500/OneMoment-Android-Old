package co.yishun.onemoment.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.convert.Converter;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.j256.ormlite.dao.Dao;
import org.androidannotations.annotations.*;

import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

//TODO stop record by MediaRecorder.setMaxDuration()

/**
 * This activity uses the camera/camcorder as the A/V source for the {@link MediaRecorder} API.
 * A {@link TextureView} is used as the camera preview which limits the code to API 14+.
 * Created by Carlos on 2/14/15.
 */
//@Fullscreen
@EActivity(R.layout.layout_recording)
public class RecordingActivity extends Activity {
    public static final int NOT_PREPARED = 0;
    public static final int PREPARED = 1;
    public static final int PREPARED_FAILED = -1;
    private static final String TAG = LogUtil.makeTag(RecordingActivity.class);
    @ViewById(R.id.surfaceView)
    TextureView mPreview;
    @ViewById
    ImageSwitcher recordFlashSwitch;
    @ViewById
    ImageSwitcher cameraSwitch;
    @ViewById
    ImageView welcomeOverlay;
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

    private boolean isAskForResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mConvertDialog = new MaterialDialog.Builder(this).content(getString(R.string.recordConvertingHint)).cancelable(false).progress(true, 0).build();

    }

    @ViewById
    FrameLayout recordSurfaceParent;

    @AfterViews
    void setSquare() {
        mPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                LogUtil.i(TAG, "onSurfaceTextureAvailable: " + width + " height: " + height);
                ViewGroup.LayoutParams params = recordSurfaceParent.getLayoutParams();
                int min = Math.min(height, width);
                LogUtil.i(TAG, "min: " + min);
                params.height = min;
                params.width = min;
                recordSurfaceParent.setLayoutParams(params);
                mPreview.setSurfaceTextureListener(null);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
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

    void checkFlashLightAvailability() {
        PackageManager packageManager = getPackageManager();

        //TODO can switch when recording?
        //TODO flashlight  http://stackoverflow.com/questions/6068803/how-to-turn-on-camera-flash-light-programmatically-in-android
        boolean hasFlash = !CameraHelper.isFrontCamera() && packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
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
        });
        recordFlashSwitch.getNextView().setOnClickListener(v -> {
            if (mCamera != null) {
                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
                recordFlashSwitch.showNext();
            }
        });
    }

    @AfterViews
    void initViews() {
//checked in onResume    checkFlashLightAvailability();
        PackageManager packageManager = getPackageManager();

        boolean hasFront = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        cameraSwitch.setEnabled(hasFront);
        cameraSwitch.setVisibility(hasFront ? View.VISIBLE : View.INVISIBLE);
        cameraSwitch.getCurrentView().setOnClickListener(hasFront ? v -> {
            CameraHelper.setFrontCamera(!CameraHelper.isFrontCamera());
            mCamera.stopPreview();
            releaseCameraBackgroundAndPreview();
        } : null);
        cameraSwitch.getNextView().setOnClickListener(hasFront ? v -> {
            CameraHelper.setFrontCamera(!CameraHelper.isFrontCamera());
            mCamera.stopPreview();
            releaseCameraBackgroundAndPreview();
        } : null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkFlashLightAvailability();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        if (!isAskForResult) {
            preview();
        } else isAskForResult = false;
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause start: " + System.currentTimeMillis());
        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCameraBackground();
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
    void releaseCameraBackground() {
        if (mCamera != null) {
            CameraHelper.releaseCamera(mCamera);
            mCamera = null;
        }
    }

    @Background
    void releaseCameraBackgroundAndPreview() {
        if (mCamera != null) {
            CameraHelper.releaseCamera(mCamera);
            mCamera = null;
        }
        preview();
    }

    //    @AfterViews
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Background
    void preview() {
        mCamera = CameraHelper.getCameraInstance();
        if (mCamera == null) return;
        LogUtil.d(TAG, "unlock at getInstance");
//        ((MyApplication) getApplication()).isRelease = false;
        final Camera.Parameters parameters = mCamera.getParameters();
        final Camera.Size optimalPreviewSize = CameraHelper.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), Config.getDefaultCameraSize().first, Config.getDefaultCameraSize().second);
        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
//        mPreview.setOpaque(false);
        this.optimalPreviewSize = optimalPreviewSize;

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
        applyTransform();
    }

    private Camera.Size optimalPreviewSize;

    @UiThread
    void applyTransform() {
        Matrix mat = calculatePreviewMatrix(mPreview, Config.getDefaultCameraSize(), optimalPreviewSize);
        mPreview.setTransform(mat);
    }

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
        mCurrentVideoPath = CameraHelper.getOutputMediaFile(this, CameraHelper.Type.RECORDED, System.currentTimeMillis()).toString();
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
        if (mCamera == null) {
            showNotification(R.string.recordLoadCameraError);
            return;
        }
        prepare();
        if (prepareStatus == PREPARED) {
            mMediaRecorder.start();
            isRecording = true;
            stopRecordAfterSomeTime();
        } else {
            releaseMediaRecorder();
        }
    }

    @UiThread
    public void showNotification(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    public void showNotification(int textRes) {
        showNotification(getString(textRes));
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
            releaseCameraBackground();
            startConvert();
        }
    }

    @UiThread
    void startConvert() {
        //TODO alert convert
        mConvertDialog.show();
        convert(mCurrentVideoPath, CameraHelper.getOutputMediaFile(this, CameraHelper.Type.LOCAL, new File(mCurrentVideoPath)).getPath());
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
            saveData(origin, converted);
        } else {
            showNotification(R.string.recordConvertError);
            LogUtil.e(TAG, "Convert Failed: ErrorCode " + exitCode);
            mConvertDialog.dismiss();
        }
    }

    public static final int REQUEST_SAVE = 100;

    @UiThread
    void askSave(String path, String thumbPath, String largeThumbPath) {
        isAskForResult = true;
        VideoSaveActivity_.intent(this)
                .extra("videoPath", path)
                .extra("thumbPath", thumbPath)
                .extra("largeThumbPath", largeThumbPath)
                .startForResult(REQUEST_SAVE);
    }

    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class)
    Dao<Moment, Integer> momentDao;


    @OnActivityResult(REQUEST_SAVE)
    void onResult(int resultCode, Intent data) {
        LogUtil.i(TAG, "onResult");
        if (resultCode == RESULT_OK) {
            //register at database
            try {
                //don't delete because it will be used at PlayAct
//                File useless = new File(data.getStringExtra("largeThumbPath"));
//                if (useless.exists()) useless.delete();

                //delete other today's moment
                String time = new SimpleDateFormat(Config.TIME_FORMAT).format(Calendar.getInstance().getTime());
                List<Moment> result = momentDao.queryBuilder().where()
                        .eq("time", time).query();
                momentDao.delete(result);

                Moment moment = new Moment.MomentBuilder()
                        .setPath(data.getStringExtra("videoPath"))
                        .setThumbPath(data.getStringExtra("thumbPath"))
                        .setLargeThumbPath(data.getStringExtra("largeThumbPath"))
                        .build();
                momentDao.create(moment);
                albumBtnClicked(null);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            isAskForResult = false;
            File useless = new File(data.getStringExtra("largeThumbPath"));
            if (useless.exists()) useless.delete();
            useless = new File(data.getStringExtra("videoPath"));
            if (useless.exists()) useless.delete();
            useless = new File(data.getStringExtra("thumbPath"));
            if (useless.exists()) useless.delete();
        }
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
            String largeThumbPath = CameraHelper.createLargeThumbImage(this, converted);

            askSave(converted, thumbPath, largeThumbPath);


        } catch (IOException e) {
            e.printStackTrace();
        }
        runOnUiThread(mConvertDialog::dismiss);
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
        if (viewHeight != viewWidth)
            LogUtil.w(TAG, "preview view width not equals height, width is " + viewWidth + ", height is " + viewHeight);
        else LogUtil.v(TAG, "view width: " + viewWidth);

        //assert rotation == 90, so width will be height
        float a = ((float) viewWidth) / size.height;
        float b = ((float) viewHeight) / size.width;
        float scaleY = a / b;
//        float scaleX = 1;
        mat.setScale(1, scaleY);

        //move to center
        mat.postTranslate(0, -viewHeight * (scaleY - 1) / 2);
//        Toast.makeText(this, "scaleX " + scaleX + "; scaleY " + scaleY, Toast.LENGTH_LONG).show();
        return mat;
    }

}
