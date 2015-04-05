package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.util.DecodeUtil;
import co.yishun.onemoment.app.util.LogUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2015/4/5.
 */
public class GetToken extends Request<GetToken.TokenResult> {
    private static final String TAG = LogUtil.makeTag(GetToken.class);
    private String fileName;

    @Override
    protected String getUrl() {
        return Config.getUrlToken();
    }

    public GetToken setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public void setCallback(final FutureCallback<TokenResult> callback) {
        check();
        if (builder != null && callback != null) {
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("filename", fileName)
                        .asString()
                        .setCallback((e, result) ->
                                callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), TokenResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void check() {
        if (fileName == null) {
            throw new IllegalStateException("null file name");
        }
    }

    public static class TokenResult implements Serializable {
        private int code;
        private String msg;
        private Data data;

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }

        public Data getData() {
            return data;
        }

        public static class Data implements Serializable {
            private String token;

            public Data() {
            }

            public String getToken() {
                return token;
            }
        }

    }
}
