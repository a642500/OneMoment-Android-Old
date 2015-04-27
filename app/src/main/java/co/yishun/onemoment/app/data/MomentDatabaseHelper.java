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
                String renameTable = "ALTER TABLE `" + Contract.DATABASE_NAME + "` RENAME TO " + Contract.Moment.TABLE_NAME + " ;";//because I made wrong with naming table with database name.
                LogUtil.i(TAG, "rename table: " + renameTable);
                dao.executeRaw(renameTable);
                String addColumn = "ALTER TABLE " + Contract.Moment.TABLE_NAME + " ADD COLUMN owner VARCHAR DEFAULT 'LOC' ;";
                LogUtil.i(TAG, "add column: " + addColumn);
                dao.executeRaw(addColumn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
