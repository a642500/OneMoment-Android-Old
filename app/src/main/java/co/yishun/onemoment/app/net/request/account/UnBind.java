package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.AccountResult;
import com.koushikdutta.async.future.FutureCallback;

/**
 * Created by Carlos on 2/15/15.
 */
public class UnBind {
    public static class Weibo extends Request<AccountResult> {
        @Override
        protected String getUrl() {
            return null;
        }

        @Override
        public void setCallback(FutureCallback<AccountResult> callback) {
            //TODO
        }

        @Override
        protected void check() {

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
