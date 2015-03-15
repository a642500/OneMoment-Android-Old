package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.net.request.Request;
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

        }

        @Override
        protected void check() {

        }
    /*
    解除微博绑定

    POST /api/v2/unbind_weibo/<account_id>

    * **Required** `<account_id> Restful形式跟在url后面`
    * **Required** `key 接口使用秘钥`
    * **Required** `weibo_uid`

    微博uid不匹配会返回WEIBO_UID_NOT_MATCH
    成功时会返回账户的信息
     */
    }

    public static class WeChat {
    /*
    解除微信绑定

    POST /api/v2/unbind_weixin/<account_id>

    * **Required** `<account_id> Restful形式跟在url后面`
    * **Required** `key 接口使用秘钥`
    * **Required** `weixin_uid`

    微信uid不匹配会返回WEIXIN_UID_NOT_MATCH
     */
    }
}
