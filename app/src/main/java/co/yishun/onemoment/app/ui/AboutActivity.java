package co.yishun.onemoment.app.ui;

import android.content.Context;
import co.yishun.onemoment.app.R;
import com.afollestad.materialdialogs.MaterialDialog;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

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
