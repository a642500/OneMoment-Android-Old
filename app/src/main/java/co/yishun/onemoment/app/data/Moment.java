package co.yishun.onemoment.app.data;

import co.yishun.onemoment.app.config.Config;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation;

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
public class Moment {
    @DatabaseField(columnName = Contract.Moment._ID, generatedId = true)
    private int id;

    @DatabaseField
    String path;

    //auto set when created
    @DatabaseField
    private long timeStamp;

    @DatabaseField
    private String time;

    public Moment() {
        //keep for ormlite
    }

    public String getPath() {
        return path;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getTime() {
        return time;
    }

    private static Moment newInstance() {
        Moment m = new Moment();
        m.timeStamp = System.currentTimeMillis();
        m.time = new SimpleDateFormat(Config.TIME_FORMAT).format(new Date());
        return m;
    }


    public static class MomentBuilder {
        private String mPath;

        public MomentBuilder setPath(String path) {
            mPath = path;
            return this;
        }

        public Moment build() {
            check();
            Moment m = Moment.newInstance();
            m.path = mPath;
            return m;
        }

        private void check() {
            if (mPath == null)
                throw new IllegalStateException("field value is error");
        }
    }
}
