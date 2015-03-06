package co.yishun.onemoment.app.recorder;

//import com.googlecode.javacv.cpp.avcodec;

/**
 * provide parameters for {@link Recorder}
 * <p/>
 * Created by kotlin on 2/4/15.
 */
public class RecorderParameters {

//    public int videoCodec = avcodec.AV_CODEC_ID_H264;

    public int videoFrameRate = 30;

    public int videoQuality = 12;

//    public int audioCodec = avcodec.AV_CODEC_ID_AAC;

    public int audioChannel = 1;

    public int audioBitRate = 96000;

    public int videoBitRate = 1000000;

    public int audioSamplingRate = 44100;

    public String videoOutputFormat = "mp4";

    public int width = 480;

    public int height = 480;

}
