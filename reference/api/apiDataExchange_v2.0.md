# 接口

返回数据格式

有错误发生时
code < 0

```
{
    'code': int//较高层面的请求结果
    'error_code': int//具体的请求结果
    'msg': str//错误信息
}
```

error_code详表

```
SUCCESS = 1
FAIL = 0
INPUT_MISSING = -1
INPUT_ERROR = -2
SERVER_ERROR = -3

API_REQUEST_TOO_FEQUENT = -5
API_KEY_IS_MISSING = -6
API_KEY_ERROR = -7
NOT_AUTH_REQUEST = -8
CANNOT_WRITE_DATABASE = -9

PHONE_IS_MISSING = -10
PHONE_FORMAT_ERROR = -11

PASSWORD_IS_MISSING = -12
PASSWORD_NOT_CORRECT = -13
PASSWORD_FORMAT_ERROR = -14

FILENAME_IS_MISSING = -15
QINIU_DELETE_FAILED = -16

WEIBO_UID_IS_MISSING = -17
WEIBO_UID_FORMAT_ERROR = -18
WEIBO_UID_EXISTS = -19

ACCOUNT_EXISTS = -20
ACCOUNT_NOT_AVAILABLE = -21
ACCOUNT_ID_IS_MISSING = -22
ACCOUNT_DOESNT_EXIST = -23

WEIXIN_UID_IS_MISSING = -24
WEIXIN_UID_FORMAT_ERROR = -25
WEIXIN_UID_IS_EXISTS = -26

PHONE_VERIFIED = -27
PHONE_VERIFY_CODE_IS_MISSING = -28
PHONE_VERIFY_CODE_WRONG = -29

SMS_SEND_FAIL = -30

NICKNAME_FORMAT_ERROR = -32
DESCRIPTION_FORMAT_ERROR = -33
GENDER_FORMAT_ERROR = -34
LOCATION_FORMAT_ERROR = -35
AVATAR_URL_ERROR = -36

NICKNAME_EXISTS = -37
PHONE_NOT_VERIFIED = -38

WEIBO_UID_NOT_MATCH = -39
WEIXIN_UID_NOT_MATCH = -40

IOS_DEVICE_TOKEN_IS_MISSING = -41
ADMIN_DYNAMIC_PASSWORD_MISSING = -42
CAPTCHA_IS_MISSING = -43

UNKNOWN_ERROR = -44
DYNAMIC_PW_EXPIRED = -45
ACCOUNT_AUTH_ERROR = -46

WORLD_ID_MISSING = -47
WORLD_DESCRIPTION_MISSING = -48
WORLD_DOESNT_EXIST = -49

WORLD_LIKED = -50

CAPTCHA_ERROR = -51
REPORT_REASON_MISSING = -52
BANNER_IMAGE_URL_MISSING = -53
BANNER_HREF_MISSING = -54
BANNER_ID_MISSING = -55

NICKNAME_EMPTY = -56
```

正常时
code = 1

```
{
    'code': 1(int),
    'msg': str,//返回信息, 英文 最好不要显示在用户界面上
    'data': 返回的数据
}
```

* 绑定微博uid


    POST /api/v2/bind_weibo/<account_id>

    * **Required** `key 接口使用秘钥`
    * **Required** `<account_id> 用户ID`
    * **Required** `weibo_uid 微博的uid`

    绑定成功后返回用户信息

* 动态获取资源域名


    GET /api/v2/resource_domain?key=<key>

    * **Required** `<key> 接口使用秘钥`

    **Return**

    ```
    {
        "code": 1,
        "msg": "get resource domain successfully",
        "data": {"domain": "http://yishun.qiniudn.com/"}
    }
    ```

* 播放视频页面(分享页面)


    `http://yishun.co/play?account_id=<account_id>`
    或者
    `http://yishun.co/play?filename=<filename>`
    account_id方式获取最新长视频分享
    filename方式，直接播放指定的文件

* 获取分享图片

    GET /share_image

    获取到的是分享的图片

* 获取分享文字

    GET /share_text?type=<type>

    * **Optional** `<type> 分享的类型`
        `friend` 分享给朋友的文字
        `long_video` 长视频分享文字

    如果没有提供<type> 返回的是**空字符串**
    返回的内容直接是字符串，不是json数据

* 添加device_token

    POST /api/v2/ios_device_token

    * **Required** `key 接口使用秘钥`
    * **Required** `device_token`

    添加device_token到数据库中
    成功时返回的数据
    **Return**

    ```
    {
        "msg": "save ios device token successfully",
        "code": 1,
        "data": {
            "device_token": "jeowiqrjioqjfoiewjfoew",
            "last_push_time": 0,
            "_id": "54a93e3711679e561a61069b",
            "create_time": 1420377655
        }
    }
    ```

账号系统

* 解除微博绑定

    POST /api/v2/unbind_weibo/<account_id>

    * **Required** `<account_id> Restful形式跟在url后面`
    * **Required** `key 接口使用秘钥`
    * **Required** `weibo_uid`

    微博uid不匹配会返回WEIBO_UID_NOT_MATCH
    成功时会返回账户的信息

* 解除微信绑定

    POST /api/v2/unbind_weixin/<account_id>

    * **Required** `<account_id> Restful形式跟在url后面`
    * **Required** `key 接口使用秘钥`
    * **Required** `weixin_uid`

    微信uid不匹配会返回WEIXIN_UID_NOT_MATCH

* 检查昵称是否存在

    POST /api/v2/check_nickname

    * **Required** `key 接口使用秘钥`
    * **Required** `nickname 昵称`

    若存在会返回NICKNAME_EXISTS的error_code
    不存在就会返回code=1
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

* 注册

	POST /api/v2/signup

	* **Required** `key 接口使用秘钥`
	* **Required** `phone 手机号`
	* **Required** `password 密码`

    注册完成后会返回用户注册的信息
    密码不会返回
    **Return**

    ```
    {
        'code': 1,
        'msg': 'signup successfully'
        'data': {
            '_id': str//24位长的id
            'phone': str
            'available': bool
            'signup_ua': str
            'signup_time': int
            'signup_ip': str
            'signin_ua': str
            'signin_time': int
            'signin_ip': str
            'nickname': str
            'email': str
            'email_validated': bool
            'introduction': str
            'avatar_url': str
            'weibo_uid': str
        }
    }
    ```

* 微博注册

    POST /api/v2/weibo_signup

    * **Required** `key 接口使用秘钥`
    * **Required** `uid 微博的uid`
    *  *Optional*  `nickname`
    *  *Optional*  `introduction`
    *  *Optional*  `gender`
    *  *Optional*  `avatar_url`
    *  *Optional*  `location`

    成功后会返回账户的信息, 格式同注册返回的格式

* 微信注册

    POST /api/v2/weixin_signup

    * **Required** `key 接口使用秘钥`
    * **Required** `uid 微博的uid`
    *  *Optional*  `nickname`
    *  *Optional*  `introduction`
    *  *Optional*  `gender`
    *  *Optional*  `avatar_url`
    *  *Optional*  `location`

    成功后会返回账户的信息, 格式同注册返回的格式

* 验证登陆

    一瞬账号登陆
    POST /api/v2/signin

    * **Required** `key 接口使用秘钥`
    * **Required** `phone 手机号`
    * **Required** `password 密码`

    成功后会返回账户的信息, 格式同注册返回的格式

* 获取账户个人信息(基础信息)

    GET /api/v2/account/<account_id>?key=<key>

    * **Required** `key 接口使用秘钥`
    * **Required** `<account_id> 账户ID 第三方登陆的UID Restful形式跟在url后面`

    根据一瞬账户的ID或者微博的ID获取账户信息
    成功后会返回账户的信息, 格式同注册返回的格式

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

* 重置密码

    POST /api/v2/reset_password

    * **Required** `key 接口使用秘钥`
    * **Required** `phone`
    * **Required** `password`

    成功后会返回账户的信息, 格式同注册返回的格式

同步视频接口

* 获取上传token

    GET /api/v2/callback_upload_token?key=<key>&filename=<filename>
    GET /api/v2/upload_token?key=<key>&filename=<filename>

    获取的是有回调的上传凭证
    **不提供没有回调的上传**
    * **Required** `<key> 接口使用秘钥`
    *  *Optional* `<filename> 上传后的文件名 默认为无文件名`

    成功时, 会返回获取的token, token有效时长: 1小时
    ```
    {
        'code': 1,
        'msg': 'get upload token successfully',
        'data': {
            'token': str//token字符串
        }
    }
    ```

* 获取用户所有视频列表(包括长视频)

    GET /api/v2/videos/<account_id>?key=<key>

    * **Required** `<key> 接口使用秘钥`
    * **Required** `<account_id> 账户 Restful形式跟在url后面`

    account_id跟在url的后面
    获取成功后返回所有视频的列表, 包括长视频
    **Return**

    ```
    {
        'code': 1,
        'msg': 'get videos successfully',
        'data': [
            {
                mimeType: "video/mp4",
                fsize: 27366,
                hash: "Fjy4XgesVSBsxob3GhOvUMJmlE7-",
                key: "201410251510058.mp4",
                putTime: 14180226715248440
            },
            {
                mimeType: "video/mp4",
                fsize: 174498,
                hash: "Fl0YSsoPwU3zutaDkd1mR-xc2b8n",
                key: "201411231425698.mp4",
                putTime: 14180199416878478
            },
            {
                mimeType: "video/mp4",
                fsize: 413301,
                hash: "FmygISGIUySRSOO4u6aPafoNpkxM",
                key: "201411261427850.mp4",
                putTime: 14180201081620948
            },
            {
                mimeType: "video/mp4",
                fsize: 402383,
                hash: "FqId2ez3GHpQxWtNt6egELYNdTH-",
                key: "201411261435265.mp4",
                putTime: 14180205656875628
            }, ...
        ]
    }
    ```

* 删除视频接口

    DELETE /api/v2/video
    POST /api/v2/delete_video

    * **Required** `key 接口使用秘钥`
    * **Required** `filename`

    删除失败时data中也会返回要删除的文件名
    删除成功时返回
    **Return**

    ```
    {
        'code': 1,
        'msg': 'delete success',
        'data': '删除时的文件名'
    }
    ```

短信验证

* 发送验证码(重发验证码也是这个接口)

    POST /api/v2/send_verify_sms

    * **Required** `key 接口使用秘钥`
    * **Required** `phone`

    **Return**
    ```
    {
        'code': 1,
        'msg': 'nickname not exists',
        'data': {
            'nickname': 'xxxx'
        }
    }
    ```

* 验证

    POST /api/v2/verify_phone

    * **Required** `key 接口使用秘钥`
    * **Required** `phone`
    * **Required** `verify_code`

    只有验证功能, 没有创建账户的功能

    **Return**

    ```
    {
        'code': 1,
        'msg':  'phone verified successfully',
        'data': {
            'phone': 'xxxxxx',
            'verify_code': 'xxxx'
        }
    }
    ```