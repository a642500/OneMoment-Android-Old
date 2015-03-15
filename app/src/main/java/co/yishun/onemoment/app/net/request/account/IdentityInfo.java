package co.yishun.onemoment.app.net.request.account;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.AccountResult;
import com.koushikdutta.async.future.FutureCallback;

/**
 * Created by Carlos on 2/15/15.
 */
public abstract class IdentityInfo {
    public static class Get extends Request<AccountResult> {
        @Override
        protected String getUrl() {
            return Config.getUrlIdentityInfoGet()
                    //TODO add account id
                    ;
        }

        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check();
            if (builder != null && callback != null) {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .as(AccountResult.class).setCallback(callback);
            }
        }

        @Override
        protected void check() {
            //do nothing
        }
    /*
    获取账户个人信息(基础信息)

    GET /api/v2/account/<account_id>?key=<key>

    * **Required** `key 接口使用秘钥`
    * **Required** `<account_id> 账户ID 第三方登陆的UID Restful形式跟在url后面`

    根据一瞬账户的ID或者微博的ID获取账户信息
    成功后会返回账户的信息, 格式同注册返回的格式
    */
    }

    public static class Update extends Request<AccountResult> {
        private String nickname;
        private String introduction;
        private String gender;
        private String avatarUrl;
        private String location;

        public Update setNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Update setIntroduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public Update setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Update setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Update setLocation(String location) {
            this.location = location;
            return this;
        }

        @Override
        protected String getUrl() {
            return Config.getUrlIdentityInfoUpdate()
                    //TODO add account id
                    ;
        }

        @Override
        public void setCallback(final FutureCallback<AccountResult> callback) {
            check();
            if (callback != null && builder != null) {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("nickname", nickname)
                        .setBodyParameter("introduction", introduction)
                        .setBodyParameter("gender", gender)
                        .setBodyParameter("avatar_url", avatarUrl)
                        .setBodyParameter("location", location)
                        .as(AccountResult.class).setCallback(callback);
            }
        }


        @Override
        protected void check() {
            //do nothing
        }
    /*
    * 修改账户个人信息(基础信息)

    PUT /api/v2/account/<account_id>
    POST /api/v2/update_account/<account_id>

    * **Required** `key 接口使用秘钥`
    * **Required** `<account_id> 账户ID Restful形式跟在url后面`
    *  *Optional*  `nickname`
    *  *Optional*  `gender`
    *  *Optional*  `introduction`
    *  *Optional*  `location`
    *  *Optional*  `avatar_url`

    加入了nickname的查重处理
    成功后会返回账户的信息, 格式同注册返回的格式

     */
    }
}
