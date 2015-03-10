package co.yishun.onemoment.app.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation;

/**
 * This bean is used in Ormlite and OrmliteProvider.
 * <p/>
 * Created by Carlos on 2/13/15.
 */
@AdditionalAnnotation.DefaultContentUri(authority = Contract.AUTHORITY, path = Contract.Moment.CONTENT_URI_PATH)
@AdditionalAnnotation.DefaultContentMimeTypeVnd(name = Contract.Moment.MIMETYPE_NAME, type = Contract.Moment.MIMETYPE_TYPE)
@DatabaseTable(tableName = Contract.DATABASE_NAME)
public class Moment {
    @DatabaseField(columnName = Contract.Moment._ID, generatedId = true)
    private int id;

    @DatabaseField String path;

    //auto set when created
    @DatabaseField
    private long timeStamp;

    public Moment() {
        //keep for ormlite
    }

    private static Moment newInstance() {
        Moment m = new Moment();
        m.timeStamp = System.currentTimeMillis();
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
