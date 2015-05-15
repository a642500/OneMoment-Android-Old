package co.yishun.onemoment.app.convert;

import android.content.Context;
import android.os.AsyncTask;
import co.yishun.onemoment.app.util.LogUtil;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
            loadLibraryProxy(context, mFFmpeg, new FFmpegLoadBinaryResponseHandler() {
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
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "proxy load exception", e);
        }
    }

    // force load armeabi-v7a, because others are removed by gradle
    private static void loadLibraryProxy(Context context, FFmpeg ffmpeg, FFmpegLoadBinaryResponseHandler handler) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        Constructor constructor = Class.forName("com.github.hiteshsondhi88.libffmpeg.FFmpegLoadLibraryAsyncTask").getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        AsyncTask<Void, Void, Boolean> task = (AsyncTask<Void, Void, Boolean>) constructor.newInstance(context, "armeabi-v7a", handler);
        task.execute();
    }

    @Override
    public Converter setHandler(FFmpegExecuteResponseHandler handler) {
        mHandler = handler;
        return this;
    }

    @Override void run() {
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



