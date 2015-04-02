package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import co.yishun.onemoment.app.R;
import com.afollestad.materialdialogs.MaterialDialog;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_about)
public class AboutActivity extends ToolbarBaseActivity {

    @Click(R.id.checkNewBtn)
    void checkNew() {
        showCheckUpdateDialog(this);
        //TODO check
    }

    public static MaterialDialog showUpdateDialog(Context context) {
        return new MaterialDialog.Builder(context).backgroundColorRes(R.color.bgUpdatedDialogColor).customView(R.layout.updated_dialog, false).show();
    }

    public static MaterialDialog showCheckUpdateDialog(Context context) {
        return new MaterialDialog.Builder(context).customView(R.layout.dialog_check, false).backgroundColorRes(R.color.bgUpdatedDialogColor).show();
    }


}
