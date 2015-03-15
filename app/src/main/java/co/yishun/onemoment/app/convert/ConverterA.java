package co.yishun.onemoment.app.convert;

import android.content.Context;
import android.support.annotation.NonNull;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.util.LogUtil;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

import java.io.*;

/**
 * Video Converter tool implemented by Ffmpeg.
 * <p/>
 * You can {@link ConverterA#with(Context)} to get instance.
 * <p/>
 * {@code Converter.with(Context).from(Input).cropToStandard().to(Output).get()}
 * <p/>
 * Created by Carlos on 3/6/15.
 */
@Deprecated
class ConverterA extends Converter {

    private static final String TAG = LogUtil.makeTag(ConverterA.class);
    private static final int IO_BUFFER_SIZE = 32256;
    private static final String CACHE_NAME = "ffmpeg";
    private final String EXE_PTAH;


    /**
     * @param context
     * @hide use {@link #ConverterA(Context)#with(Context)} to get instance
     */
    ConverterA(@NonNull Context context) {
        EXE_PTAH = install(context);
        mCommand.append(EXE_PTAH);
    }


    @Override public Converter setHandler(FFmpegExecuteResponseHandler handler) {
        //Don't support.
        return this;
    }

    void run() {
        new ProcessBuilder(mCommand.toString());
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
            LogUtil.i(TAG, "setExecutable: " + (ffmpegFile.setExecutable(true) ? "success" : "failed"));
            LogUtil.i(TAG, "setWritable: " + (ffmpegFile.setWritable(true) ? "success" : "failed"));
            LogUtil.i(TAG, "setReadable: " + (ffmpegFile.setReadable(true) ? "success" : "failed"));

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
