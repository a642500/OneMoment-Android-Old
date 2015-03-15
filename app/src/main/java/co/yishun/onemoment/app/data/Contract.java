package co.yishun.onemoment.app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for database and provider.
 * <p/>
 * Created by Carlos on 3/9/15.
 */
public class Contract {

    public static final String DATABASE_NAME = "OneDataBase";
    public static final int DATABASE_VERSION = 1;
    public static final String AUTHORITY = "co.yishun.onemoment.app.data.moment";


    /**
     * Moment table info
     */
    public static class Moment implements BaseColumns {
        public static final String TABLE_NAME = "moments";
        public static final String CONTENT_URI_PATH = TABLE_NAME;

        public static final String MIMETYPE_TYPE = TABLE_NAME;
        public static final String MIMETYPE_NAME = AUTHORITY + "provider";

        public static final String PATH = "path";
        //TODO ??
        public static final int CONTENT_URI_PATTERN_MANY = 1;
        public static final int CONTENT_URI_PATTERN_ONE = 2;

        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .appendPath(CONTENT_URI_PATH)
                .build();

    }

}
