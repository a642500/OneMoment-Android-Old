package co.yishun.onemoment.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import co.yishun.onemoment.app.util.CameraHelper.Type;
import co.yishun.onemoment.app.util.LogUtil;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * SQLite opener helper for Moment.
 * <p>
 * Created by Carlos on 3/9/15.
 */
public class MomentDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = LogUtil.makeTag(MomentDatabaseHelper.class);

    public MomentDatabaseHelper(Context context) {
        super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Moment.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * own default value: {@link Type#LOCAL}'s {@link Type#getPrefix(Context)}
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            if (oldVersion == 1) {
                Dao<Moment, Integer> dao = getDao(Moment.class);
                LogUtil.i(TAG, "upgrade database from " + oldVersion + " to " + newVersion);
                String raw = "ALTER TABLE `" + Contract.Moment.TABLE_NAME + "` ADD COLUMN owner VARCHAR DEFAULT 'LOC' ;";
                 LogUtil.i(TAG, "affected rows: " + dao.executeRaw(raw));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
