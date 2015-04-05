package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

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
        check();
        if (builder != null && callback != null) {
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .asString()
                        .setCallback((e, result) ->
                                callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), VideoListResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void check() {
        //do nothing
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
