package co.yishun.onemoment.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import co.yishun.onemoment.app.Fun;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.convert.Converter;
import co.yishun.onemoment.app.data.Moment;
import co.yishun.onemoment.app.data.MomentDatabaseHelper;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.umeng.analytics.MobclickAgent;
import me.toxz.circularprogressview.library.CircularProgressView;
import org.androidannotations.annotations.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static co.yishun.onemoment.app.ui.RecordingActivity.RecordStatus.*;

//TODO stop record by MediaRecorder.setMaxDuration()

/**
 * This activity uses the camera/camcorder as the A/V source for the {@link MediaRecorder} API.
 * A {@link TextureView} is used as the camera preview which limits the code to API 14+.
 * Created by Carlos on 2/14/15.
 */
//@Fullscreen
@EActivity(R.layout.layout_recording)
public class RecordingActivity extends Activity {
    public static final int REQUEST_SAVE = 100;
    private static final String TAG = LogUtil.makeTag(RecordingActivity.class);
    @ViewById(R.id.surfaceView) TextureView mPreview;
    @ViewById ImageSwitcher recordFlashSwitch;
    @ViewById ImageSwitcher cameraSwitch;
    @ViewById ImageView welcomeOverlay;
    @ViewById(R.id.recordVideoBtn) CircularProgressView circularProgressView;
    @ViewById ImageButton albumBtn;
    @ViewById FrameLayout recordSurfaceParent;
    @OrmLiteDao(helper = MomentDatabaseHelper.class, model = Moment.class) Dao<Moment, Integer> momentDao;
    private RecordStatus mStatus = RecordStatus.CAMERA_NOT_PREPARED;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private String mCurrentVideoPath = null;
    /*
    Dialog to display convert progress.
     */
    private Dialog mConvertDialog;
    private Camera.Size optimalPreviewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mConvertDialog = new MaterialDialog.Builder(this).theme(Theme.DARK).content(getString(R.string.recordConvertingHint)).cancelable(false).progress(true, 0).build();
    }

    /**
     * set the preview surface to square
     */
    @AfterViews void setSquare() {
        mPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                LogUtil.i(TAG, "onSurfaceTextureAvailable: " + width + " height: " + height);
                ViewGroup.LayoutParams params = recordSurfaceParent.getLayoutParams();
                int min = Math.min(height, width);
                LogUtil.i(TAG, "min: " + min);
                params.height = min;
                params.width = min;
                recordSurfaceParent.setLayoutParams(params);
                mPreview.setSurfaceTextureListener(null);
                mPreview.setOnClickListener(v -> {
                    if (mStatus.ordinal() >= PREVIEW_PREPARED.ordinal() && mStatus.ordinal() <= RECORDER_PREPARED.ordinal()) {
                        mCamera.autoFocus(null);
                    }
                });
            }

            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }

            @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
        });
    }

    @Fun @AfterViews void initCircularProgressView() {
        circularProgressView.setOnStateListener(status -> {
                    LogUtil.v(TAG, "onStatus: " + status.name());
                    switch (status) {
                        case START:
                            switch (mStatus) {
                                case CAMERA_PREPARED://no break
                                case PREVIEW_PREPARED:

                                    //disable/hide button
                                    setCameraSwitchEnable(false);
                                    Animation a = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                                    a.setDuration(300);
                                    a.setAnimationListener(new Animation.AnimationListener() {
                                        @Override public void onAnimationStart(Animation animation) {/*do nothing*/}

                                        @Override
                                        public void onAnimationEnd(Animation animation) { albumBtn.setVisibility(View.INVISIBLE); }

                                        @Override public void onAnimationRepeat(Animation animation) {/*do nothing*/}
                                    });
                                    albumBtn.startAnimation(a);

                                    record();
                                    circularProgressView.setDuration(1200);
                                    break;
                                case CAMERA_NOT_PREPARED:
                                    showNotification(R.string.recordLoadCameraError);
                                    break;
                                default://do nothing
                                    break;
                            }
                            break;
                        case END:
                            //TODO remove video save act
                            break;
                    }
                }
        );
    }

    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Fun
    @Click void albumBtnClicked(View view) {
        //TODO
        new AlbumActivity_.IntentBuilder_(this).start();
    }

    @UiThread void checkFlashLightAvailability() {
        PackageManager packageManager = getPackageManager();

        //TODO flashlight  http://stackoverflow.com/questions/6068803/how-to-turn-on-camera-flash-light-programmatically-in-android
        boolean hasFlash = !CameraHelper.isFrontCamera() && packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        recordFlashSwitch.setEnabled(hasFlash);
        recordFlashSwitch.setVisibility(hasFlash ? View.VISIBLE : View.INVISIBLE);
        recordFlashSwitch.setDisplayedChild(0);
    }

    @UiThread void checkFrontCameraLightAvailability() {
        PackageManager packageManager = getPackageManager();
        boolean hasFront = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        cameraSwitch.setEnabled(hasFront);
        cameraSwitch.setVisibility(hasFront ? View.VISIBLE : View.INVISIBLE);
        if (hasFront) {
            View.OnClickListener listener = v -> {
                //delay to let animation end, but disable itself to prevent post many switch event
                setCameraSwitchEnable(false);
                circularProgressView.setEnabled(false);
                mStatus = CAMERA_NOT_PREPARED;
                switchCamera();
            };
            cameraSwitch.getCurrentView().setOnClickListener(listener);
            cameraSwitch.getNextView().setOnClickListener(listener);
        }

    }

    @Background(delay = 150) void switchCamera() {
        CameraHelper.setFrontCamera(!CameraHelper.isFrontCamera());
//        recordFlashSwitch.setEnabled(false);
        mCamera.stopPreview();
        releaseCameraAndPreview();
    }

    @AfterViews void initViews() {
        checkFrontCameraLightAvailability();

        recordFlashSwitch.getCurrentView().setOnClickListener(v -> {
            try {
                if ((mStatus.compareTo(CAMERA_PREPARED) >= 0 && mStatus.compareTo(RECORDER_NOT_PREPARED) < 0)
                        || (mStatus.compareTo(RECORDER_STARTED) >= 0 && mStatus.compareTo(RECORDER_ENDED) < 0)) {
                    Camera.Parameters p = mCamera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(p);
                    recordFlashSwitch.showNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        recordFlashSwitch.getNextView().setOnClickListener(v -> {
            try {
                if (mStatus.compareTo(CAMERA_PREPARED) >= 0 && mStatus.compareTo(RECORDER_ENDED) < 0) {
                    Camera.Parameters p = mCamera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(p);
                    recordFlashSwitch.showNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //checked in onResume    checkFlashLightAvailability();
    }

    @UiThread void setCameraSwitchEnable(boolean enable) {
        if (cameraSwitch != null) {
            cameraSwitch.setEnabled(enable);
            cameraSwitch.getCurrentView().setEnabled(enable);
            cameraSwitch.getNextView().setEnabled(enable);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_SYSTEM, true);

        MobclickAgent.onResume(this);
        checkFlashLightAvailability();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        if (mStatus == RecordStatus.MOMENT_OK) {
            circularProgressView.resetSmoothly();
            setCameraSwitchEnable(true);//was disabled when start record
        }
        if (mStatus != RecordStatus.CONVERTER_ENDED && mStatus != MOMENT_PREPARED) {
            //just back for result, not preview
            preview();
        }
        if (mStatus == MOMENT_PREPARED) {
            mStatus = MOMENT_OK;
            LogUtil.d(TAG, "resume at: " + MOMENT_PREPARED);
        }

    }

    @Override protected void onPause() {
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_SYSTEM, false);
        LogUtil.d(TAG, "onPause start: " + System.currentTimeMillis());
        super.onPause();
        MobclickAgent.onPause(this);
        // if we are using MediaRecorder, release it first
        if (mStatus.compareTo(CAMERA_PREPARED) >= 0 && mStatus.compareTo(RECORDER_ENDED) <= 0)
            mStatus = CAMERA_NOT_PREPARED;
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCameraBackground();
        LogUtil.d(TAG, "onPause end: " + System.currentTimeMillis());
    }

    @Override protected void onDestroy() {
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

    @Background void releaseCameraBackground() {
        if (mCamera != null) {
            CameraHelper.releaseCamera(mCamera);
            mCamera = null;
        }
    }

    @SupposeBackground void releaseCameraAndPreview() {
        if (mCamera != null) {
            CameraHelper.releaseCamera(mCamera);
            mCamera = null;
        }
        preview();
        checkFlashLightAvailability();
        delayEnable();
    }

    @UiThread void delayEnable() {
        setCameraSwitchEnable(true);
        circularProgressView.setEnabled(true);
    }

    //    @AfterViews
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Background void preview() {
        //get camera
        assert mStatus == RecordStatus.CAMERA_NOT_PREPARED || mStatus == RecordStatus.ERROR_STATUS || mStatus == RecordStatus.MOMENT_OK;
        mCamera = CameraHelper.getCameraInstance();
        if (mCamera == null) {
            mStatus = RecordStatus.ERROR_STATUS;
            return;
        } else {
            mStatus = CAMERA_PREPARED;
        }

        //preview
        final Camera.Parameters parameters = mCamera.getParameters();
        final Camera.Size optimalPreviewSize = CameraHelper.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), Config.getDefaultCameraSize().first, Config.getDefaultCameraSize().second);

        //use default height and width of profile instead
        CamcorderProfile profile = getProfile();
        optimalPreviewSize.width = profile.videoFrameWidth;
        optimalPreviewSize.height = profile.videoFrameHeight;

        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
        this.optimalPreviewSize = optimalPreviewSize;
        try {

            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
            mCamera.startPreview();
            applyTransform();
            mStatus = RecordStatus.PREVIEW_PREPARED;
        } catch (IOException e) {
            LogUtil.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            mStatus = RecordStatus.ERROR_STATUS;
        } catch (RuntimeException e) {
            LogUtil.e(TAG, "Camera is being used after Camera.release() was called" + e.getMessage());
            mStatus = RecordStatus.ERROR_STATUS;
        }
//        mCamera.setOneShotPreviewCallback((data, camera) -> hideSplash());
        delayEnable();
    }

    @UiThread void applyTransform() {
        Matrix mat = calculatePreviewMatrix(mPreview, Config.getDefaultCameraSize(), optimalPreviewSize);
        mPreview.setTransform(mat);
    }

    @UiThread void hideSplash() {
        welcomeOverlay.setVisibility(View.GONE);
    }

    private CamcorderProfile getProfile() {
        CamcorderProfile profile = null;
        int[] allProfiles = {
                CamcorderProfile.QUALITY_480P,
                CamcorderProfile.QUALITY_TIME_LAPSE_480P,

                CamcorderProfile.QUALITY_720P,
                CamcorderProfile.QUALITY_TIME_LAPSE_720P,
                CamcorderProfile.QUALITY_LOW,
                CamcorderProfile.QUALITY_TIME_LAPSE_LOW,
                CamcorderProfile.QUALITY_QCIF,
                CamcorderProfile.QUALITY_TIME_LAPSE_QCIF,
                CamcorderProfile.QUALITY_CIF,
                CamcorderProfile.QUALITY_TIME_LAPSE_CIF,

                CamcorderProfile.QUALITY_HIGH,
                CamcorderProfile.QUALITY_TIME_LAPSE_HIGH,
                CamcorderProfile.QUALITY_1080P,
                CamcorderProfile.QUALITY_TIME_LAPSE_1080P,

        };
        for (int oneProfile : allProfiles) {
            if (CamcorderProfile.hasProfile(oneProfile)) {
                profile = CamcorderProfile.get(oneProfile);
                break;
            }
        }
        if (profile == null) throw new IllegalArgumentException("no profile at all!!");
        return profile;
    }

    /**
     * Asynchronous task for preparing the {@link MediaRecorder} since it's a long blocking
     * operation.
     * <p>
     * This method starts from {@link RecordStatus#RECORDER_NOT_PREPARED} to {@link RecordStatus#RECORDER_PREPARED} or {@link RecordStatus#ERROR_STATUS}.
     */
    @SupposeBackground void prepare() {
        mStatus = RecordStatus.RECORDER_NOT_PREPARED;

        //prepare recorder
        Camera.Parameters parameters = mCamera.getParameters();
        /*
        Huawei Mate 7 return null when call getSupportedVideoSizes(),
        but return an list contain some not support sizes when getSupportedPreviewSizes().
        So, we must handle this case specially
         */
        List<Camera.Size> sizeList = parameters.getSupportedVideoSizes();
        if (sizeList == null) sizeList = parameters.getSupportedPreviewSizes();
        Camera.Size optimalVideoSize = CameraHelper.getOptimalPreviewSize(sizeList, 480, 480);

        CamcorderProfile profile = getProfile();
        // use profile's default width and height, because some laji three-party android system return unsupported size in getSupportedPreviewSizes, like Huawei phone
        optimalVideoSize.width = profile.videoFrameWidth;
        optimalVideoSize.height = profile.videoFrameHeight;

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
        mMediaRecorder.setOrientationHint(CameraHelper.isFrontCamera() ? 270 : 90);
        mMediaRecorder.setMaxDuration(1200);
        mMediaRecorder.setOnInfoListener((mr, what, extra) ->
                {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
                        onRecordEnd();
                }
        );
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
            mStatus = RecordStatus.RECORDER_PREPARED;
        } catch (IllegalStateException e) {
            LogUtil.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            mStatus = RecordStatus.ERROR_STATUS;
        } catch (IOException e) {
            LogUtil.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            mStatus = RecordStatus.ERROR_STATUS;
        }
    }

    /**
     * It prepares the {@link MediaRecorder} and starts recording.
     * <p>
     * It asserts that camera was prepared and {@link RecordingActivity#mStatus} is {@link RecordStatus#CAMERA_PREPARED} or {@link RecordStatus#PREVIEW_PREPARED}
     */
    @Background void record() {
        LogUtil.v(TAG, "record (background)");
        assert mStatus == CAMERA_PREPARED || mStatus == RecordStatus.PREVIEW_PREPARED;
        prepare();
        if (mStatus == RecordStatus.RECORDER_PREPARED) {
            mStatus = RecordStatus.RECORDER_STARTED;
            Moment.lock(this);
            mMediaRecorder.start();
        } else {
            releaseMediaRecorder();
            //TODO recorder prepare failed
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

    private void onRecordEnd() {
        LogUtil.i(TAG, "time run out");
        if (mStatus == RecordStatus.RECORDER_STARTED) {
            mStatus = RecordStatus.RECORDER_ENDED;
            // release media recorder and  camera
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            releaseCameraBackground();
            startConvert();
        }
    }

    @UiThread(delay = 1000) void startConvert() {
        //TODO change convert notification method
        mConvertDialog.show();
        mStatus = CONVERTER_STARTED;
        convert(mCurrentVideoPath, CameraHelper.getOutputMediaFile(this, CameraHelper.Type.LOCAL, new File(mCurrentVideoPath)).getPath());
    }

    @Background(delay = 300) void convert(String from, String to) {
        Converter.with(this).from(from).cropToStandard().to(to).setHandler(new FFmpegExecuteResponseHandler() {
            @Override public void onSuccess(String message) {
                LogUtil.i(TAG, "success: " + message);
                onConvert(0, from, to);
            }

            @Override public void onProgress(String message) {
                LogUtil.i(TAG, "progress: " + message);

            }

            @Override public void onFailure(String message) {
                LogUtil.e(TAG, "fail: " + message);
                onConvert(1, from, to);
            }

            @Override public void onStart() {
                LogUtil.i(TAG, "start");
                LogUtil.i(TAG, "Converter2 onStart time: " + System.currentTimeMillis());

            }

            @Override public void onFinish() {
                LogUtil.i(TAG, "Converter2 onFinish time: " + System.currentTimeMillis());
                LogUtil.i(TAG, "finish");
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

    @UiThread void onConvert(int exitCode, String origin, String converted) {
//        setCameraSwitchEnable(true);
        albumBtn.setVisibility(View.VISIBLE);//TODO add animation
        if (0 == exitCode) {
            saveData(origin, converted);
        } else {
            mStatus = ERROR_STATUS;

            showNotification(R.string.recordConvertError);
            LogUtil.e(TAG, "Convert Failed: ErrorCode " + exitCode);
            circularProgressView.resetSmoothly();
            preview();
            mConvertDialog.dismiss();
        }
    }

    @Background void saveData(String origin, String converted) {
        //delete origin file
        File file = new File(origin);
        if (file.exists() && new File(converted).exists()) {
            file.delete();
        }

        try {
            //create and save thumb image
            String thumbPath = CameraHelper.createThumbImage(this, converted);
            String largeThumbPath = CameraHelper.createLargeThumbImage(this, converted);
            mStatus = CONVERTER_ENDED;
            askSave(converted, thumbPath, largeThumbPath);
        } catch (IOException e) {
            e.printStackTrace();
            mStatus = ERROR_STATUS;
        }
        runOnUiThread(mConvertDialog::dismiss);
    }

    @UiThread void askSave(String path, String thumbPath, String largeThumbPath) {
        VideoSaveActivity_.intent(this)
                .extra("videoPath", path)
                .extra("thumbPath", thumbPath)
                .extra("largeThumbPath", largeThumbPath)
                .startForResult(REQUEST_SAVE);
    }

    @OnActivityResult(REQUEST_SAVE) void onResult(int resultCode, Intent data) {
        LogUtil.i(TAG, "onResult");
        mStatus = MOMENT_PREPARED;
        if (resultCode == RESULT_OK) {
            //register at database
            try {
                //don't delete because it will be used at PlayAct
//                File useless = new File(data.getStringExtra("largeThumbPath"));
//                if (useless.exists()) useless.delete();

                //delete other today's moment
                String time = new SimpleDateFormat(Config.TIME_FORMAT).format(Calendar.getInstance().getTime());
                List<Moment> result;
                Where<Moment, Integer> w = momentDao.queryBuilder().where();
                if (AccountHelper.isLogin(this)) {
                    result = w.and(
                            w.eq("time", time), w.or(
                                    w.eq("owner", AccountHelper.getIdentityInfo(this).get_id()),
                                    w.eq("owner", "LOC"))
                    ).query();
                } else {
                    result = w.eq("time", time).and().eq("owner", "LOC").query();
                }
                LogUtil.i(TAG, "delete old today moment: " + Arrays.toString(result.toArray()));

                Moment moment = new Moment.MomentBuilder()
                        .setPath(data.getStringExtra("videoPath"))
                        .setThumbPath(data.getStringExtra("thumbPath"))
                        .setLargeThumbPath(data.getStringExtra("largeThumbPath"))
                        .build();
                if (1 == momentDao.create(moment)) {
                    LogUtil.i(TAG, "new moment: " + moment);
                    momentDao.delete(result);
                    for (Moment mToDe : result) {
                        new File(mToDe.getLargeThumbPath()).delete();
                        new File(mToDe.getThumbPath()).delete();
                        new File(mToDe.getPath()).delete();
                    }
                }
                // onResult is called before onResume
                LogUtil.d(TAG, "start album act");
                Moment.unlock();
                mCurrentVideoPath = null;
                albumBtnClicked(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mStatus = MOMENT_OK;
            File useless = new File(data.getStringExtra("largeThumbPath"));
            if (useless.exists()) useless.delete();
            useless = new File(data.getStringExtra("videoPath"));
            if (useless.exists()) useless.delete();
            useless = new File(data.getStringExtra("thumbPath"));
            if (useless.exists()) useless.delete();
            mCurrentVideoPath = null;
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

    enum RecordStatus implements Comparable<RecordStatus> {
        CAMERA_NOT_PREPARED,
        CAMERA_PREPARED,
        PREVIEW_PREPARED,
        RECORDER_NOT_PREPARED,
        RECORDER_PREPARED,
        RECORDER_STARTED,
        RECORDER_ENDED,
        CONVERTER_STARTED,
        CONVERTER_ENDED,//converted, will ask user whether save it
        MOMENT_PREPARED,//has asked for saving, will jump to album act
        MOMENT_OK,//has created a new moment or give up the moment,just back to this act to create new
        ERROR_STATUS
    }

}
