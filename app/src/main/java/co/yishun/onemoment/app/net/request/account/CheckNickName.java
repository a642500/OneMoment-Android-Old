package co.yishun.onemoment.app.net.request.account;

import android.text.TextUtils;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.Request;
import co.yishun.onemoment.app.net.result.NickNameResult;
import co.yishun.onemoment.app.util.DecodeUtil;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;

import java.util.concurrent.ExecutionException;

/**
 * Created by Carlos on 2/15/15.
 */
public class CheckNickName extends Request<NickNameResult> {
    private String nickname;

    public CheckNickName setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Override
    protected String getUrl() {
        return Config.getUrlCheckNickname();
    }

    @Override
    public void setCallback(FutureCallback<NickNameResult> callback) {
        check();
        if (builder != null && callback != null) {
            try {
                builder.load(getUrl())
                        .setBodyParameter("key", key)
                        .setBodyParameter("nickname", nickname).asString().setCallback((e, result) ->
                                callback.onCompleted(e, new Gson().fromJson(DecodeUtil.decode(result), NickNameResult.class))
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void check() {
        if (nickname == null || TextUtils.isEmpty(nickname)) {
            throw new IllegalStateException("error nick name");
        }
    }
    /*

    POST /api/v2/check_nickname

    * **Required** `key
    * **Required** `nickname`

    if exist: NICKNAME_EXISTS
    note exist: return code=1
    **Return**

    ```
    {
        "msg": "nickname not exists",
        "code": 1,
        "data": {
            "nickname": "111"
        }
    }
    ```
     */
}
