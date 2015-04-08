package co.yishun.onemoment.app.convert;

import android.content.Context;
import android.support.annotation.NonNull;
import co.yishun.onemoment.app.util.LogUtil;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

/**
 * Created by Carlos on 3/7/15.
 */
public abstract class Converter {
    private static final String TAG = LogUtil.makeTag(Converter.class);
    final StringBuilder mCommand = new StringBuilder();
    private String mOutput;


    /**
     * Get Converter instance.
     */
    public static Converter with(Context context) {
        return new Converter2(context);
    }

    /**
     * Add a arg to ffmpeg.
     *
     * @param arg to be added.
     * @return Converter to call in chain.
     */
    public Converter arg(@NonNull final String arg) {
        mCommand.append(' ');
        mCommand.append(arg);
        return this;
    }

    /**
     * add input file arg, for example: "-i /sdcard/test.mp4". You can add many input args.
     *
     * @param inputFile path to input file
     * @return Converter to call in chain.
     */
    public Converter from(@NonNull final String inputFile) {
        mCommand.append(" -i ");
        mCommand.append(inputFile);
        return this;
    }

    /**
     * set output file arg, you can only set one output file arg.
     *
     * @param outFile path to output file
     * @return Converter to call in chain.
     */
    public Converter to(@NonNull final String outFile) {
        mOutput = outFile;
        return this;
    }

    /**
     * add default crop arg, to center crop with 480*480.
     *
     * @return Converter to call in chain.
     */
    public Converter cropToStandard() {
        mCommand.append(" -vf");
        mCommand.append(" crop='if(gt(iw,ih),ih,iw)':'if(gt(iw,ih),ih,iw)',scale=480:480");
        mCommand.append(" -r 30");//fps
        mCommand.append(" -b:v 4M");//bitrate


        mCommand.append(" -aspect 1");//ratio
        mCommand.append(" -vcodec h264");
        mCommand.append(" -b:a 128k");

        mCommand.append(" -y");//overwrite output files
        mCommand.append(" -t 1.2");// set the recording time

        mCommand.append(" -strict");
        mCommand.append(" -2");
        return this;
    }

    /**
     * Get the command.
     *
     * @return the command.
     */
    public String getCommand() {
        return mCommand.toString();
    }


    public abstract Converter setHandler(FFmpegExecuteResponseHandler handler);

    /**
     * to get command ready and let it run.
     */
    final public void start() {
        mCommand.append(' ');
        mCommand.append(mOutput);
        LogUtil.i(TAG, this.toString());
        run();
    }

    @Override
    public String toString() {
        return getCommand();
    }

    /**
     * exec command.
     */
    abstract void run();

    public static class NotSupportMethod extends Throwable {

    }
}
