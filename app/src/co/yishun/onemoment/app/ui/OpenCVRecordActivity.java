package co.yishun.onemoment.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.CameraHelper;
import org.androidannotations.annotations.*;

import java.io.IOException;

/**
 * This is an version of {@link RecordingActivity} implement by OpenCV.
 * <p/>
 * Created by Carlos on 2/26/15.
 */
@EActivity(R.layout.recording_layout)
public class OpenCVRecordActivity extends Activity {
    private Camera mCamera;
    @ViewById(R.id.surfaceView)
    TextureView mPreview;
    private MediaRecorder mMediaRecorder;

    private boolean isRecording = false;
    private static final String TAG = "Recorder";
//    @ViewById(R.id.recordVideoBtn)
//    ImageButton captureButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases {@link android.media.MediaRecorder} and {@link android.hardware.Camera}. When not recording,
     * it prepares the {@link android.media.MediaRecorder} and starts recording.
     *
     * @param view the view generating the event.
     */
    @Click(R.id.recordVideoBtn)
    public void onCaptureClick(View view) {
        if (!isRecording) {
//            new MediaPrepareTask().execute(null, null, null);
            record();
        }
    }


    private void setCaptureButtonText(String title) {
//        captureButton.setText(title);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
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

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    private int prepareStatus = NOT_PREPARED;
    public static final int NOT_PREPARED = 0;
    public static final int PREPARED = 1;
    public static final int PREPARED_FAILED = -1;

    @AfterViews
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Background void preview() {
        // BEGIN_INCLUDE (configure_preview)
        mCamera = CameraHelper.getDefaultCameraInstance();
        Camera.Parameters parameters = mCamera.getParameters();


        Camera.Size optimalPreviewSize = CameraHelper.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), mPreview.getWidth(), mPreview.getHeight());
        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            prepareStatus = PREPARED_FAILED;
        }

        mCamera.startPreview();
    }

    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    @SupposeBackground void prepare() {
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
        mMediaRecorder.setOutputFile(CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setOrientationHint(90);
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            prepareStatus = PREPARED_FAILED;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            prepareStatus = PREPARED_FAILED;
        }
        prepareStatus = PREPARED;
    }

    @Background void record() {
        prepare();
        if (prepareStatus == PREPARED) {
            mMediaRecorder.start();
            isRecording = true;
            stopRecordAfterSomeTime();
        } else {
            releaseMediaRecorder();
        }
    }

    @UiThread(delay = 1200L) void stopRecordAfterSomeTime() {
        Log.i(TAG, "time run out, isRecording: " + isRecording);
        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();
            Toast.makeText(this, "ok!!", Toast.LENGTH_LONG).show();
        }
    }

}
