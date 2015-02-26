package co.yishun.onemoment.app.deprecated;

import com.googlecode.javacv.cpp.avcodec;

/**
 * This class contain all the information about the audio and video.
 * <p/>
 * Created by Carlos on 2/3/15.
 */
public class RecorderParameters {
    public static final int videoCodec = avcodec.AV_CODEC_ID_H264;

    public static final int videoFrameRate = 30;

    public static final int videoQuality = 12;

    public static final int audioCodec = avcodec.AV_CODEC_ID_AAC;

    public static final int audioChannel = 1;

    public static final int audioBitrate = 96000;

    public static final int videoBitrate = 1000000;

    public static final int audioSamplingRate = 44100;

    public static final String videoOutputFormat = "mp4";

    public static final int width = 480;

    public static final int height = 480;

}
