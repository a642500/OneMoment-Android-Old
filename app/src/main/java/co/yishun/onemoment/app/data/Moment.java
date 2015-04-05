package co.yishun.onemoment.app.data;

import android.content.Context;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.sync.Data;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This bean is used in Ormlite and OrmliteProvider.
 * <p>
 * Created by Carlos on 2/13/15.
 */
@AdditionalAnnotation.DefaultContentUri(authority = Contract.AUTHORITY, path = Contract.Moment.CONTENT_URI_PATH)
@AdditionalAnnotation.DefaultContentMimeTypeVnd(name = Contract.Moment.MIMETYPE_NAME, type = Contract.Moment.MIMETYPE_TYPE)
@DatabaseTable(tableName = Contract.DATABASE_NAME)
public class Moment implements Serializable {
    @DatabaseField
    String path;
    @DatabaseField
    String thumbPath;
    @DatabaseField
    String largeThumbPath;
    @DatabaseField(columnName = Contract.Moment._ID, generatedId = true)
    private int id;
    //auto set when created
    @DatabaseField
    private long timeStamp;

    @DatabaseField
    private String time;

    public Moment() {
        //keep for ormlite
    }

    private static Moment newInstance() {
        Moment m = new Moment();
        m.timeStamp = System.currentTimeMillis();
        m.time = new SimpleDateFormat(Config.TIME_FORMAT).format(new Date());
        return m;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return new File(path);
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getTime() {
        return time;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public String getLargeThumbPath() {
        return largeThumbPath;
    }

    public static class MomentBuilder {
        private static final String TAG = LogUtil.makeTag(MomentBuilder.class);
        private String mPath;
        private String mThumbPath = null;
        private String mLargeThumbPath = null;

        public MomentBuilder setPath(String path) {
            mPath = path;
            return this;
        }

        public MomentBuilder setThumbPath(String path) {
            mThumbPath = path;
            return this;
        }

        public MomentBuilder setLargeThumbPath(String path) {
            mLargeThumbPath = path;
            return this;
        }

        public Moment build() {
            check();
            Moment m = Moment.newInstance();
            m.path = mPath;
            m.thumbPath = mThumbPath;
            m.largeThumbPath = mLargeThumbPath;
            return m;
        }

        private void check() {
            if (mPath == null)
                throw new IllegalStateException("field value is error");
            if (mThumbPath == null) LogUtil.w(TAG, "null thumb path");
            if (mLargeThumbPath == null) LogUtil.w(TAG, "null large thumb path");
        }
    }

    public static Moment from(Data video, String path, String thumbPath, String largeThumbPath) {
        Moment m = new Moment();
        m.path = path;
        m.thumbPath = thumbPath;
        m.largeThumbPath = largeThumbPath;
        m.timeStamp = video.getTimeStamp();
        m.time = video.getTime();
        return m;
    }

    public static Moment parseOf(Context context, long timeStamp) {
        return new MomentBuilder()
                .setPath(CameraHelper.getOutputMediaPath(context, CameraHelper.Type.LOCAL, timeStamp))
                .setThumbPath(CameraHelper.getOutputMediaPath(context, CameraHelper.Type.MICRO_THUMB, timeStamp))
                .setLargeThumbPath(CameraHelper.getOutputMediaPath(context, CameraHelper.Type.LARGE_THUMB, timeStamp))
                .build();
    }
}
