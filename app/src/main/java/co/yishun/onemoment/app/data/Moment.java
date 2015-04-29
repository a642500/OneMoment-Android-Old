package co.yishun.onemoment.app.data;

import android.content.Context;
import android.support.annotation.Nullable;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.net.request.sync.Data;
import co.yishun.onemoment.app.util.CameraHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This bean is used in Ormlite and OrmliteProvider.
 * <p>
 * Created by Carlos on 2/13/15.
 */
@AdditionalAnnotation.DefaultContentUri(authority = Contract.AUTHORITY, path = Contract.Moment.CONTENT_URI_PATH)
@AdditionalAnnotation.DefaultContentMimeTypeVnd(name = Contract.Moment.MIMETYPE_NAME, type = Contract.Moment.MIMETYPE_TYPE)
@DatabaseTable(tableName = Contract.Moment.TABLE_NAME)
public class Moment implements Serializable {
    private static final String TAG = LogUtil.makeTag(Moment.class);
    private static FileChannel channel;

    //    public final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static FileLock lock;


    public static void lock(Context context) {
        try {
            if (channel == null) {
                File f = new File(context.getDir(Config.IDENTITY_DIR, Context.MODE_PRIVATE), "tmp.lock");
                if (!f.exists()) f.createNewFile();
                channel = new FileOutputStream(f).getChannel();
            }
            if (lock == null) {
                LogUtil.d(TAG, "lock at thread: " + Thread.currentThread().getId());
                lock = channel.lock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unlock() {
        try {
            lock.release();
            lock = null;
            channel.close();
            channel = null;
            LogUtil.d(TAG, "unlock at thread: " + Thread.currentThread().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DatabaseField String path;
    @DatabaseField String thumbPath;
    @DatabaseField String largeThumbPath;
    @DatabaseField(columnName = Contract.Moment._ID, generatedId = true) private int id;
    //auto set when created
    @DatabaseField private long timeStamp;
    /**
     * add at database version 2.0
     */
    @DatabaseField private String owner;

    @DatabaseField private String time;

    public Moment() { /*keep for ormlite*/ }

    private static Moment newInstance() {
        Moment m = new Moment();
        m.timeStamp = System.currentTimeMillis();
        m.time = new SimpleDateFormat(Config.TIME_FORMAT).format(new Date());
        return m;
    }

    /**
     * Set the owner of the moment, null to set it public.
     *
     * @param owner id of the owner
     */
    public void setOwner(@Nullable String owner) {
        if (owner == null) {
            this.owner = "LOC";
        } else this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isPublic() {
        return owner.startsWith("LOC");
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() { return path; }

    public File getFile() { return new File(path); }

    public long getTimeStamp() { return timeStamp; }

    public String getTime() { return time; }

    public String getThumbPath() { return thumbPath; }

    public String getLargeThumbPath() { return largeThumbPath; }

    public static class MomentBuilder {
        private static final String TAG = LogUtil.makeTag(MomentBuilder.class);
        private String mPath;
        private String mThumbPath = null;
        private String mLargeThumbPath = null;
        private String mOwner = "LOC";//have default value

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

        /**
         * set owner of the moment, null to set public
         *
         * @param userId
         * @return
         */
        public MomentBuilder setOwner(@Nullable String userId) {
            if (userId == null) {
                mOwner = "LOC";
            } else mOwner = userId;
            return this;
        }

        public Moment build() {
            check();
            Moment m = Moment.newInstance();
            m.path = mPath;
            m.thumbPath = mThumbPath;
            m.largeThumbPath = mLargeThumbPath;
            m.owner = mOwner;
            return m;
        }

        private void check() {
            if (mPath == null) throw new IllegalStateException("field value is error");
            if (mThumbPath == null) LogUtil.w(TAG, "null thumb path");
            if (mLargeThumbPath == null) LogUtil.w(TAG, "null large thumb path");
            if (mOwner == null) {
                LogUtil.e(TAG, "null owner, has set LOC");
                mOwner = "LOC";
            }
        }
    }

    public void setLargeThumbPath(String largeThumbPath) {
        this.largeThumbPath = largeThumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    /**
     * Create a private moment from {@link Data}
     *
     * @param video
     * @param path
     * @param thumbPath
     * @param largeThumbPath
     * @return
     */
    public static Moment from(Data video, String path, String thumbPath, String largeThumbPath) {
        Moment m = new Moment();
        m.path = path;
        m.thumbPath = thumbPath;
        m.largeThumbPath = largeThumbPath;
        m.timeStamp = video.getTimeStamp();
        m.time = video.getTime();
        m.setOwner(video.getUserID());
        return m;
    }

    public static Moment parseOf(Context context, long timeStamp) {
        return new MomentBuilder()
                .setPath(CameraHelper.getOutputMediaPath(context, CameraHelper.Type.LOCAL, timeStamp))
                .setThumbPath(CameraHelper.getOutputMediaPath(context, CameraHelper.Type.MICRO_THUMB, timeStamp))
                .setLargeThumbPath(CameraHelper.getOutputMediaPath(context, CameraHelper.Type.LARGE_THUMB, timeStamp))
                .build();
    }

    @Override
    public String toString() {
        return "Moment{" +
                "path='" + path + '\'' +
                ", owner='" + owner + '\'' +
                ", time='" + time + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
