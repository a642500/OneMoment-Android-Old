package co.yishun.onemoment.app.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Carlos on 2015/4/2.
 */
@EActivity(R.layout.activity_integrate_info)
public class IntegrateInfoActivity extends BaseActivity {
    public static final String TAG = LogUtil.makeTag(IntegrateInfoActivity.class);
    public static final int REQUEST_PHONE = 0;
    public static final int REQUEST_WEIBO = 1;

    public static final int RESULT_CANCEL = 0;
    public static final int RESULT_SUCCESS = 1;


//    public static final String EXTRA_SIGN_UP_TYPE = "type";
//    public static final String EXTRA_PHONE = "phone";

//    public enum SignUpType {phone, weibo}

    @Extra
    String phone;
    @Extra
    String password;

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
                .itemsCallbackSingleChoice(MALE, (dialog, view1, which, text) -> {
                    setGender(which);
                    return true; // allow selection
                })
                .positiveText(R.string.integrateInfoChooseBtn)
                .show();
    }

    @Click
    void okBtnClicked(View view) {

    }


}
