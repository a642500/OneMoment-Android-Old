package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Carlos on 2015/4/2.
 */
@EActivity(R.layout.activity_integrate_info)
public class IntegrateInfoActivity extends ActionBarActivity {
    public static final String TAG = LogUtil.makeTag(IntegrateInfoActivity.class);

    public static final String EXTRA_SIGN_UP_TYPE = "type";
    public static final String EXTRA_PHONE = "phone";

    public enum SignUpType {phone, weibo}

    private SignUpType mType;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        mType = (SignUpType) intent.getSerializableExtra(EXTRA_SIGN_UP_TYPE);
        switch (mType) {
            case phone:
                mPhone = intent.getStringExtra(EXTRA_PHONE);
                break;
            case weibo:

                break;
            default:
                LogUtil.e(TAG, "unknown sign up type");
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(getString(R.string.integrateInfoTitle));
        toolbar.setNavigationOnClickListener(v -> IntegrateInfoActivity.this.onBackPressed());
    }

    @ViewById
    EditText nickNameEditText;


    @Click
    void areaItemClicked(View view) {

    }


    public static final int MALE = 0;
    public static final int FEMALE = 1;

    @ViewById
    TextView genderTextView;

    private void setGender(int gender) {
        switch (gender) {
            case MALE:
                genderTextView.setText(String.valueOf('\u2642'));
                break;
            case FEMALE:
                genderTextView.setText(String.valueOf('\u2640'));
                break;
            default:
                LogUtil.e(TAG, "unknown gender!!");
                break;
        }
    }

    @Click
    void genderItemClicked(View view) {
        new MaterialDialog.Builder(this)
                .theme(Theme.DARK)
                .title(R.string.integrateInfoGenderHint)
                .items(R.array.integrateInfoGenderArray)
                .itemsCallbackSingleChoice(MALE, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        setGender(which);
                        return true; // allow selection
                    }
                })
                .positiveText(R.string.integrateInfoChooseBtn)
                .show();
    }

    @Click
    void okBtnClicked(View view) {

    }

}
