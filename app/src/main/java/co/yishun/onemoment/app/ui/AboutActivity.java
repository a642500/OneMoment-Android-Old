package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import co.yishun.onemoment.app.R;
import com.afollestad.materialdialogs.MaterialDialog;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_about)
public class AboutActivity extends ToolbarBaseActivity {

    public static MaterialDialog showUpdateDialog(Context context) {
        return new MaterialDialog.Builder(context).backgroundColorRes(R.color.bgUpdatedDialogColor).customView(R.layout.dialog_updated, false).show();
    }

    public static MaterialDialog showCheckUpdateDialog(Context context) {
        return new MaterialDialog.Builder(context).customView(R.layout.dialog_check, false).backgroundColorRes(R.color.bgUpdatedDialogColor).show();
    }

    @Click(R.id.checkNewBtn)
    void checkNew() {
        showCheckUpdateDialog(this);
        //TODO check
    }

    @Click
    void recommendBtnClicked(View view) {
        Intent sendIntent = new Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, getString(R.string.aboutRecommendContent)).setType("text/plain");
        startActivity(sendIntent);
    }

}
