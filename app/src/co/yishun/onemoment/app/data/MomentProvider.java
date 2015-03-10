package co.yishun.onemoment.app.data;

import com.tojc.ormlite.android.OrmLiteSimpleContentProvider;
import com.tojc.ormlite.android.framework.MatcherController;
import com.tojc.ormlite.android.framework.MimeTypeVnd.SubType;

/**
 * Content Provider to
 * Created by Carlos on 2/22/15.
 */
public class MomentProvider extends OrmLiteSimpleContentProvider<MomentDatabaseHelper> {

    @Override protected Class<MomentDatabaseHelper> getHelperClass() {
        return MomentDatabaseHelper.class;
    }

    @Override public boolean onCreate() {
        setMatcherController(new MatcherController()
                        .add(Contract.Moment.class, SubType.DIRECTORY, "", Contract.Moment.CONTENT_URI_PATTERN_MANY)
                        .add(Contract.Moment.class, SubType.ITEM, "#", Contract.Moment.CONTENT_URI_PATTERN_ONE)
        );
        return true;
    }
}