package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

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
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .asString()
                        .setCallback((e, result) ->
                                callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), DomainResult.class))).wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
