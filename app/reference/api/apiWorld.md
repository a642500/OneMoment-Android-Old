# 1.4 世界接口
测试接口地址: http://test.yishun.co/

* 举报世界接口 POST /api/v2/report_world

    * **Required** `key 接口使用秘钥`
    * **Required** `world_id 被举报世界的ID`
    * **Required** `account_id 举报者的ID`
    * **Required** `reason 举报理由`
    *  *Optional*  `url 外链地址 默认为None`
    *  *Optional*  `type 举报类型 默认为world 表示举报世界`

    不会检测world和account是否存在
    **Return**

    ```
    {
        "msg": "report successfully",
        "code": 1,
        "data": {
            "reason": "fjieajfoa",
            "_id": "54bcacf348feda9d68777a99",
            "world_id": "ea",
            "account_id": "ejaif"
        }
    }
    ```

* 获取banner列表 GET /api/v2/world_banners?key=<key>&limit=<limit>

    * **Required** `<key> 接口使用秘钥`
    *  *Optional*  `<limit> 返回数量限制 默认3`

    **Return**

    ```
    {
        msg: "get banners successfully",
        code: 1,
        data: [
            {
                create_time: 1421470204,
                href: "http://www.baidu.com/",
                image_url: "http://www.baidu.com/img/bdlogo.png"
            }
        ]
    }
    ```

* 获取世界列表 GET /api/v2/worlds?key=<key>&account_id=<account_id>&offset=<offset>&limit=<limit>

    * **Required** `<key> 接口使用秘钥`
    *  *Optional*  `<offset> 偏移量 默认0`
    *  *Optional*  `<limit> 返回数量限制 默认10`
    *  *Optional*  `<account_id> 用户的ID, 用来判断是否点过赞`

    **Return**

    ```
    {
        msg: "get worlds successfully",
        code: 1,
        data: [
            {
                update_time: 1421380996,
                videos_num: 4, // 即是多少人参与（不管重复问题）
                account_id: "54b284e248feda036fb30a18",
                create_time: 1421380996,
                like_num: 1,
                _id: "54b88d8448feda6954b21a28",
                thumbnail: null, // 世界的截图
                description: "test",
                liked: (true|false)
            },
            {
                update_time: 1421381547,
                videos_num: 1,
                account_id: "54b88f8548feda69d4895d2b",
                create_time: 1421381547,
                like_num: 0,
                _id: "54b88fab48feda69d4895d2c",
                thumbnail: null,
                description: "hhhhh",
                liked: (true|false)
            }
        ]
    }
    ```

* 获取一个世界的视频列表 GET /api/v2/world/<world_id>?key=<key>&account_id=<account_id>&offset=<offset>&limit=<limit>

    * **Required** `<key> 接口使用秘钥`
    * **Required** `<world_id> 世界的ID`
    *  *Optional*  `<offset> 偏移量 默认0`
    *  *Optional*  `<limit> 返回数量限制 默认10`
    *  *Optional*  `<account_id> 用户的ID, 用来判断是否点过赞`

    **Return**

    ```
    {
        msg: "get world video successfully",
        code: 1,
        data: {
            'world': {
                "update_time": 1421380996,
                "videos_num": 5,
                "account_id": "54b284e248feda036fb30a18",
                "create_time": 1421380996,
                "like_num": 0,
                "_id": "54b88d8448feda6954b21a28",
                "thumbnail": null,
                "description": "test",
                "liked": (true|false)
            },
            'videos': [
                {
                    world_id: "54b88d8448feda6954b21a28",
                    _id: "54b88d9b48feda6954b21a29",
                    create_time: 1421381019,
                    account_id: null,
                    filename: "123123123",
                    account_avatar_url: ""//用户头像的地址
                },
                {
                    world_id: "54b88d8448feda6954b21a28",
                    _id: "54b88dc948feda6960b6154f",
                    create_time: 1421381065,
                    account_id: null,
                    filename: "123123123",
                    account_avatar_url: ""//用户头像的地址
                },
                {
                    world_id: "54b88d8448feda6954b21a28",
                    _id: "54b88dd348feda696b3c68f3",
                    create_time: 1421381075,
                    account_id: null,
                    filename: "123123123",
                    account_avatar_url: ""//用户头像的地址
                },
                {
                    world_id: "54b88d8448feda6954b21a28",
                    _id: "54b88e1048feda69759a7870",
                    create_time: 1421381136,
                    account_id: "54b284e248feda036fb30a18",
                    filename: "123123123",
                    account_avatar_url: ""//用户头像的地址
                }
            ]
        }
    }
    ```

* 我发起的 GET /api/v2/worlds_created/<account_id>?key=<key>&offset=<offset>&limit=<limit>

    * **Required** `<key> 接口使用秘钥`
    * **Required** `<account_id> 用户ID`
    *  *Optional*  `<offset> 偏移量 默认0`
    *  *Optional*  `<limit> 返回世界的限制数量 默认10`

    **Return**

    ```
    {
        msg: "get account created world successfully",
        code: 1,
        data: [
            {
                update_time: 1421380996,
                videos_num: 4,
                account_id: "54b284e248feda036fb30a18",
                create_time: 1421380996,
                like_num: 1,
                _id: "54b88d8448feda6954b21a28",
                thumbnail: null,
                description: "test"
            }
        ]
    }
    ```

* 我参与的 GET /api/v2/worlds_joined/<account_id>?key=<key>&offset=<offset>&limit=<limit>

    * **Required** `<key> 接口使用秘钥`
    * **Required** `<account_id> 用户ID`
    *  *Optional*  `<offset> 偏移量 默认0`
    *  *Optional*  `<limit> 返回世界的限制数量 默认10`

    ```
    {
        msg: "get account joined world successfully",
        code: 1,
        data: [
            {
                update_time: 1421381547,
                videos_num: 1,
                account_id: "54b88f8548feda69d4895d2b",
                create_time: 1421381547,
                like_num: 0,
                _id: "54b88fab48feda69d4895d2c",
                thumbnail: null,
                description: "hhhhh"
            }
        ]
    }
    ```


* 发布一个世界 POST /api/v2/world

    * **Required** `key 接口使用秘钥`
    * **Required** `account_id 发布者ID`
    * **Required** `description 对世界的描述`

    ```
    {
        "msg": "create new world successfully",
        "code": 1,
        "data": {
            "update_time": 1421384088,
            "videos_num": 0,
            "account_id": "54b284e248feda036fb30a18",
            "create_time": 1421384088,
            "like_num": 0,
            "_id": "54b8999848feda6a3ffa8fec",
            "thumbnail": null,
            "description": "hhhhh"
        }
    }
    ```

* 向一个世界添加视频 POST /api/v2/world_video/<world_id>

    * **Required** `key 接口使用秘钥`
    * **Required** `<world_id> 添加到世界的ID`
    * **Required** `account_id 发布视频者的ID`
    * **Required** `filename 视频文件名`

    **Return**

    ```
    {
        "msg": "post video to world successfully",
        "code": 1,
        "data": {
            "world": {
                "update_time": 1421380996,
                "videos_num": 5,
                "account_id": "54b284e248feda036fb30a18",
                "create_time": 1421380996,
                "like_num": 1,
                "_id": "54b88d8448feda6954b21a28",
                "thumbnail": null,
                "description": "test"
            },
            "account": {
                "signin_time": null,
                "weixin_nickname": null,
                "signin_ip": null,
                "introduction": null,
                "signup_ip": "127.0.0.1",
                "avatar_url": "http://yishun.qiniudn.com/avatar-default.png",
                "location": null,
                "available": true,
                "weibo_nickname": null,
                "phone": "18502710852",
                "phone_verified": true,
                "nickname": null,
                "signup_ua": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36",
                "gender": "n",
                "weibo_uid": null,
                "weixin_uid": null,
                "signin_ua": null,
                "signup_time": 1420985570,
                "_id": "54b284e248feda036fb30a18"
            },
            "world_video": {
                "world_id": "54b88d8448feda6954b21a28",
                "_id": "54b89a6b48feda6a3ffa8fed",
                "create_time": 1421384299,
                "account_id": "54b284e248feda036fb30a18",
                "filename": "hhhhhh"
            }
        }
    }
    ```

* 给世界点赞 POST /api/v2/world/like/<world_id>

    * **Required** `key 接口使用秘钥`
    * **Required** `<world_id> 赞的世界ID`
    * **Required** `account_id 点赞人的ID`

    **Return**
    如果对点赞过的世界进行点赞，就会报`WORLD_LIKED`错误

    ```
    {
        "msg": "unlike world successfully",
        "code": 1,
        "data": {
            "world": {
                "update_time": 1421380996,
                "videos_num": 5,
                "account_id": "54b284e248feda036fb30a18",
                "create_time": 1421380996,
                "like_num": 0,
                "_id": "54b88d8448feda6954b21a28",
                "thumbnail": null,
                "description": "test"
            },
            "account": {
                "signin_time": null,
                "weixin_nickname": null,
                "signin_ip": null,
                "introduction": null,
                "signup_ip": "127.0.0.1",
                "avatar_url": "http://yishun.qiniudn.com/avatar-default.png",
                "location": null,
                "available": true,
                "weibo_nickname": null,
                "phone": "18502710852",
                "phone_verified": true,
                "nickname": null,
                "signup_ua": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36",
                "gender": "n",
                "weibo_uid": null,
                "weixin_uid": null,
                "signin_ua": null,
                "signup_time": 1420985570,
                "_id": "54b284e248feda036fb30a18"
            }
        }
    }
    ```

* 取消赞 POST /api/v2/world/unlike/<world_id>

    * **Required** `key 接口使用秘钥`
    * **Required** `<world_id> 世界ID`
    * **Required** `account_id 点赞人的ID`

    **Return**

    ```
    {
        "msg": "unlike world successfully",
        "code": 1,
        "data": {
            "world": {
                "update_time": 1421380996,
                "videos_num": 5,
                "account_id": "54b284e248feda036fb30a18",
                "create_time": 1421380996,
                "like_num": 0,
                "_id": "54b88d8448feda6954b21a28",
                "thumbnail": null,
                "description": "test"
            },
            "account": {
                "signin_time": null,
                "weixin_nickname": null,
                "signin_ip": null,
                "introduction": null,
                "signup_ip": "127.0.0.1",
                "avatar_url": "http://yishun.qiniudn.com/avatar-default.png",
                "location": null,
                "available": true,
                "weibo_nickname": null,
                "phone": "18502710852",
                "phone_verified": true,
                "nickname": null,
                "signup_ua": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36",
                "gender": "n",
                "weibo_uid": null,
                "weixin_uid": null,
                "signin_ua": null,
                "signup_time": 1420985570,
                "_id": "54b284e248feda036fb30a18"
            }
        }
    }
    ```