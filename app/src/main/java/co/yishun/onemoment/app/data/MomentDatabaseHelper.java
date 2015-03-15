package co.yishun.onemoment.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * SQLite opener helper for Moment.
 * <p/>
 * Created by Carlos on 3/9/15.
 */
public class MomentDatabaseHelper extends OrmLiteSqliteOpenHelper {

    public MomentDatabaseHelper(Context context) {
        super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Moment.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Moment.class, true);
            TableUtils.createTable(connectionSource, Moment.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
