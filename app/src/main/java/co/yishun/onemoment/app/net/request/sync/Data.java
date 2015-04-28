package co.yishun.onemoment.app.net.request.sync;

import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.util.LogUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Carlos on 2015/4/5.
 */
public class Data implements Serializable, Comparable<Data>, Map.Entry<Integer, Data> {
    private static final String TAG = LogUtil.makeTag(Data.class);
    private String mimeType;
    private String fsize;
    private String hash;
    private String key;
    private String putTime;

    //<userID>-<time>-<timestamp>.mp4
    public Data() {
    }

    public String getFsize() {
        return fsize;
    }

    public String getHash() {
        return hash;
    }

    public String getQiuniuKey() {
        LogUtil.i(TAG, "qiniu key: " + key);
        return key;
    }

    @Override
    public Integer getKey() {
        return Integer.parseInt(getTime());
    }

    public String getTime() {
        return this.key.substring(key.indexOf(Config.URL_HYPHEN) + 1, key.lastIndexOf(Config.URL_HYPHEN));
    }

    @Override
    public Data getValue() {
        return this;
    }

    public String getUserID() {
        String id = this.key.substring(0, key.indexOf(Config.URL_HYPHEN));
        LogUtil.v(TAG, "getUserID: " + id);
        return id;
    }

    @Override
    public Data setValue(Data object) {
        return null;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getPutTime() {
        return putTime;
    }

    public long getTimeStamp() {
        return Long.parseLong(key.substring(key.lastIndexOf(Config.URL_HYPHEN) + 1, key.lastIndexOf(".")));
    }

    @Override
    public int compareTo(Data another) {
        return this.getKey() - another.getKey();
    }

    @Override
    public String toString() {
        return "Data{" +
                "key='" + key + '\'' +
                ", putTime='" + putTime + '\'' +
                '}';
    }
}
