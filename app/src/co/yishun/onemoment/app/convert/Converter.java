package co.yishun.onemoment.app.convert;

import android.content.Context;
import android.support.annotation.NonNull;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.util.LogUtil;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Video Converter tool implemented by Ffmpeg.
 * <p/>
 * You can {@link Converter#with(Context)} to get instance.
 * <p/>
 * {@code Converter.with(Context).from(Input).cropToStandard().to(Output).get()}
 * <p/>
 * Created by Carlos on 3/6/15.
 */
public class Converter {

    private static final String TAG = LogUtil.makeTag(Converter.class);
    private static final int IO_BUFFER_SIZE = 32256;
    private static final String CACHE_NAME = "ffmpeg";
    private final String EXE_PTAH;
    private final List<String> mCommand;
    private String mOutput;


    /**
     * @param context
     * @hide use {@link #Converter(Context)#with(Context)} to get instance
     */
    private Converter(@NonNull Context context) {
        EXE_PTAH = install(context);
        mCommand = new LinkedList<>();
        mCommand.add(EXE_PTAH);
    }

    /**
     * Get Converter instance.
     *
     * @param context
     * @return
     */
    public static Converter with(@NonNull Context context) {
        return new Converter(context);
    }

    /**
     * Add a arg to ffmpeg.
     *
     * @param arg to be added.
     * @return Converter to call in chain.
     */
    public Converter arg(final String arg) {
        mCommand.add(arg);
        return this;
    }

    /**
     * Get process with set command.
     *
     * @return the process
     */
    public ProcessBuilder get() {
        if (mOutput != null) {
            mCommand.add(mOutput);
        }
        LogUtil.i(TAG, Arrays.toString(mCommand.toArray()));
        return new ProcessBuilder(mCommand);
    }

    /**
     * add input file arg, for example: "-i /sdcard/test.mp4". You can add many input args.
     *
     * @param inputFile path to input file
     * @return Converter to call in chain.
     */
    public Converter from(String inputFile) {
        mCommand.add("-i");
        mCommand.add(inputFile);
        return this;
    }

    /**
     * set output file arg, you can only set one output file arg.
     *
     * @param outputFile path to output file
     * @return Converter to call in chain.
     */
    public Converter to(String outputFile) {
        mOutput = outputFile;
        return this;
    }

    /**
     * add default crop arg, to center crop with 480*480.
     *
     * @return Converter to call in chain.
     */
    public Converter cropToStandard() {
        mCommand.add("-vf");
        mCommand.add("crop=480,480");
        mCommand.add("-strict");
        mCommand.add("-2");
        return this;
    }


    /**
     * to install executable ffmpeg.
     *
     * @param context
     * @return path to executable ffmpeg, null when error occurred when installing.
     */
    private String install(@NonNull Context context) {
        //TODO avoid to install every time instantiate.
        String path = null;
        final InputStream rawSteam = context.getResources().openRawResource(Config.getFfmpegRawId());
        try {
            File ffmpegFile = checkFile(context);
            final OutputStream binStream = new FileOutputStream(ffmpegFile);
            byte[] buffer = new byte[IO_BUFFER_SIZE];
            int count;
            while ((count = rawSteam.read(buffer)) > 0) {
                binStream.write(buffer, 0, count);
            }
            ffmpegFile.setExecutable(true);
            path = ffmpegFile.toString();

            try {
                rawSteam.close();
                binStream.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "Failed to close stream", e);
            }
        } catch (FileNotFoundException e) {
            LogUtil.e(TAG, "Cache file not found.", e);
        } catch (IOException e) {
            LogUtil.e(TAG, "Write error", e);
        }

        return path;
    }

    private File checkFile(Context context) throws IOException {
        File ffmpegFile = new File(context.getCacheDir(), CACHE_NAME);
        LogUtil.i(TAG, "ffmpeg install path: " + ffmpegFile.toString());

        if (!ffmpegFile.exists()) {
            ffmpegFile.createNewFile();
        }
        return ffmpegFile;
    }
}
