package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2/15/15.
 */
public class UnBind {
    public static class Weibo extends Request<AccountResult> {

        /*
    POST /api/v2/unbind_weibo/<account_id>

    * **Required** `<account_id> Restful形式跟在url后面`
    * **Required** `key 接口使用秘钥`
    * **Required** `weibo_uid`
         */

        @Override
        protected String getUrl() {
            return Config.getUrlUnbindWeibo() + AccountHelper.getIdentityInfo(mContext).get_id();
        }

        private String uid;

        public Weibo setUid(String uid) {
            this.uid = uid;
            return this;
        }

        @Override
        public void setCallback(FutureCallback<AccountResult> callback) {
            check(callback);
            try {
                builder.load(getUrl()).setBodyParameter("key", key).setBodyParameter("weibo_uid", uid)
                        .asString().setCallback((e, result) -> callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), AccountResult.class))).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void check(FutureCallback<AccountResult> callback) {
            if (builder == null) {
                throw new IllegalStateException("null builder");
            }
            if (callback == null) {
                throw new IllegalArgumentException("null callback");
            }
            if (uid == null) throw new IllegalStateException("null builder");

        }
    /*

    POST /api/v2/unbind_weibo/<account_id>

    * **Required** `<account_id> Restful`
    * **Required** `key`
    * **Required** `weibo_uid`

     */
    }

    public static class WeChat {
    /*

    POST /api/v2/unbind_weixin/<account_id>

    * **Required** `<account_id> Restful`
    * **Required** `key `
    * **Required** `weixin_uid`

     */
    }
}
