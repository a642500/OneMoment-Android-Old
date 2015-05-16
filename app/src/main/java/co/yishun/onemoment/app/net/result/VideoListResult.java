package co.yishun.onemoment.app.net.result;

import co.yishun.onemoment.app.net.request.sync.Data;

import java.io.Serializable;

/**
 * Created by yyz on 5/16/15.
 */
public class VideoListResult implements Serializable {
    private int code;
    private String msg;
    private Data[] data;


    public int getCode() {
        return code;
    }

    public Data[] getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

}
