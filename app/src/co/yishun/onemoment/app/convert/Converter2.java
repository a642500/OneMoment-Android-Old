package co.yishun.onemoment.app.convert;

import android.content.Context;
import android.util.Log;
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
    private String mOutput;
    private final FFmpeg mFFmpeg;
    private final StringBuilder mCommand = new StringBuilder();
    private FFmpegExecuteResponseHandler mHandler;

    Converter2(Context context) {
        mFFmpeg = FFmpeg.getInstance(context);
        try {
            mFFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override public void onFailure() {
                    Log.e(TAG, "load error");
                }

                @Override public void onSuccess() {
                    Log.i(TAG, "load success");
                }

                @Override public void onStart() {
                    Log.i(TAG, "load start");
                }

                @Override public void onFinish() {
                    Log.i(TAG, "load finish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }


    @Override public Converter setHandler(FFmpegExecuteResponseHandler handler) {
        mHandler = handler;
        return this;
    }

    @Override void run() {
        try {
            mFFmpeg.execute(mCommand.toString(), mHandler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}



