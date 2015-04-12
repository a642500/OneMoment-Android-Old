package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.util.DecodeUtil;
import co.yishun.onemoment.app.util.LogUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Serializable;

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
        check(callback);
        try {
            OkHttpClient client = new OkHttpClient();
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder().url(getUrl() + "?key=" + key + "&filename=" + fileName)
                    .get().build();
            Response response = client.newCall(request).execute();

            callback.onCompleted(null, new Gson().fromJson(DecodeUtil.decode(response.body().string()), TokenResult.class));
//    GET /api/v2/upload_token?key=<key>&filename=<filename>
        } catch (IOException e) {
            callback.onCompleted(e, null);
        }
    }

    @Override
    protected void check(FutureCallback<TokenResult> callback) {
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
