package co.yishun.onemoment.app.recorder;

/**
 * Created by kotlin on 2/4/15.
 */
public class RecorderException extends Exception {
    public RecorderException(String detailMessage) {
        super(detailMessage);
    }
}

class IllegalStateRecorderException extends RecorderException {
    public IllegalStateRecorderException(String detailMessage) {
        super(detailMessage);
    }
}

class CameraInitRecorderException extends RecorderException {
    public CameraInitRecorderException(String detailMessage) {
        super(detailMessage);
    }
}