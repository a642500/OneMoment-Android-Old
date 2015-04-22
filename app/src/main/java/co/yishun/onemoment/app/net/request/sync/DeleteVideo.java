package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2015/4/5.
 */
public class DeleteVideo extends Request<DeleteVideo.DeleteResult> {
    private String fileName;

    @Override
    protected String getUrl() {
        return Config.getUrlVideoDelete();
    }

    public DeleteVideo setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }


    @Override
    public void setCallback(FutureCallback<DeleteResult> callback) {
        check(callback);
        try {
            builder.load(getUrl())
                    .setBodyParameter("key", key)
                    .setBodyParameter("filename", fileName)
                    .asString()
                    .setCallback((e, result) ->
                            callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), DeleteResult.class))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void check(FutureCallback<DeleteResult> callback) {
        if (builder == null) {
            throw new IllegalStateException("null builder");
        }
        if (callback == null) {
            throw new IllegalArgumentException("null callback");
        }
        if (fileName == null) {
            throw new IllegalStateException("null file name");
        }
    }

    public static class DeleteResult implements Serializable {
        private int code;
        private String msg;
        private Data data;//file name deleted

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }

        public Data getData() {
            return data;
        }

        class Data {
            String filename;
        }
    }
}
