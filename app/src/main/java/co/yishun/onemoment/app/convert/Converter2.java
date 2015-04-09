package co.yishun.onemoment.app.convert;

import android.content.Context;
import co.yishun.onemoment.app.util.LogUtil;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

/**
 * Created by Carlos on 3/7/15.
 */
class Converter2 extends Converter {

    private static final String TAG = LogUtil.makeTag(Converter2.class);
    private final FFmpeg mFFmpeg;
    private FFmpegExecuteResponseHandler mHandler;

    Converter2(Context context) {
        LogUtil.i(TAG, "Converter2 get instance time: " + System.currentTimeMillis());
        mFFmpeg = FFmpeg.getInstance(context);
        try {
            mFFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    LogUtil.e(TAG, "load error");
                }

                @Override
                public void onSuccess() {
                    LogUtil.i(TAG, "load success");
                }

                @Override
                public void onStart() {
                    LogUtil.i(TAG, "load start");
                }

                @Override
                public void onFinish() {
                    LogUtil.i(TAG, "load finish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Converter setHandler(FFmpegExecuteResponseHandler handler) {
        mHandler = handler;
        return this;
    }

    @Override
    void run() {
        try {
            String cmd = mCommand.toString();
            LogUtil.i(TAG, "cmd: " + cmd);
            LogUtil.i(TAG, "Converter2 exec start time: " + System.currentTimeMillis());
            mFFmpeg.execute(cmd, mHandler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}



