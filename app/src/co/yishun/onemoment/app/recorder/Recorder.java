package co.yishun.onemoment.app.recorder;

import android.view.SurfaceView;

/**
 * A proxy to record video and audio and
 * <p/>
 * Created by kotlin on 2/4/15.
 */
public interface Recorder {

    /**
     * notify Recorder to check status and init camera device.
     * You should invoke it at background.
     *
     * @throws IllegalStateRecorderException when you haven't set {@link RecorderParameters}
     */
    void init() throws CameraInitRecorderException, IllegalStateRecorderException;

    /**
     * start preview.
     *
     * @return the SurfaceView to display preview, you should place it in your layout.
     */
    SurfaceView startPreview();

    void stopPreview();

    /**
     * start record.
     *
     * @throws IllegalStateRecorderException when recording has been running.
     */
    void startRecord() throws IllegalStateRecorderException;

    /**
     * give up recent record
     *
     * @throws IllegalStateRecorderException when recording is not running.
     */
    void stopRecord() throws IllegalStateRecorderException;

    RecorderParameters getParams();

    Recorder setParams(RecorderParameters parameters) throws IllegalStateRecorderException;

    RecordWriter getWriter();

    /**
     * to set {@link RecordWriter} to
     *
     * @return this recorder instance
     */
    Recorder setWriter(RecordWriter writer);

    /**
     * release all resource.
     */
    void release();

    void setOnRecordStatusListener(OnRecordStatusListener listener);

    interface OnRecordStatusListener {

        void onRecordFinish(boolean success);

        void onSaveFinish(boolean success);
    }
}




