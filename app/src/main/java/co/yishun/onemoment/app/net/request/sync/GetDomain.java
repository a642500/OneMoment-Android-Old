package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Serializable;

/**
 * <strong>block thread</strong>
 * <p>
 * Created by Carlos on 2015/4/5.
 */
public class GetDomain extends Request<GetDomain.DomainResult> {


    @Override
    protected String getUrl() {
        return Config.getUrlDomain();
    }

    @Override
    public void setCallback(FutureCallback<DomainResult> callback) {
        check();
        if (builder != null && callback != null) {
            try {
                OkHttpClient client = new OkHttpClient();
                com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder().url(getUrl() + "?key=" + key)
                        .get().build();
                Response response = client.newCall(request).execute();

                callback.onCompleted(null, new Gson().fromJson(DecodeUtil.decode(response.body().string()), DomainResult.class));
            } catch (IOException e) {
                callback.onCompleted(e, null);
            }
        }
    }

    @Override
    protected void check() {
        //do nothing
    }


    public static class DomainResult implements Serializable {
        private int code;
        private String msg;
        private Data data;

        public int getCode() {
            return code;
        }

        public Data getData() {
            return data;
        }

        public String getMsg() {
            return msg;
        }

        public static class Data implements Serializable {
            private String domain;

            public String getDomain() {
                return domain;
            }
        }
    }
}
