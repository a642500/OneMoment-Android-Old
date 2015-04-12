package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Carlos on 2015/4/5.
 */
public class GetVideoList extends Request<GetVideoList.VideoListResult> {

    @Override
    protected String getUrl() {
        return Config.getUrlVideoList() + AccountHelper.getIdentityInfo(mContext).get_id();
    }

    @Override
    public void setCallback(FutureCallback<VideoListResult> callback) {
        check(callback);
        try {
            OkHttpClient client = new OkHttpClient();
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder().url(getUrl() + "?key=" + key)
                    .get().build();
            Response response = client.newCall(request).execute();

            callback.onCompleted(null, new Gson().fromJson(DecodeUtil.decode(response.body().string()), VideoListResult.class));
        } catch (IOException e) {
            callback.onCompleted(e, null);
        }
    }

    @Override
    protected void check(FutureCallback<VideoListResult> callback) {
        if (builder == null) {
            throw new IllegalStateException("null builder");
        }
        if (callback == null) {
            throw new IllegalArgumentException("null callback");
        }
    }

    public static class VideoListResult implements Serializable {
        private int code;
        private String msg;
        private Data[] data;


        public int getCode() {
            return code;
        }

        public Data[] getDatas() {
            return data;
        }

        public String getMsg() {
            return msg;
        }


    }
}
