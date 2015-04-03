package co.yishun.onemoment.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_identity_info)
public class IdentityInfoActivity extends ToolbarBaseActivity {
    public static final int REQUEST_UPDATE_INFO = 0;
    private static final String TAG = LogUtil.makeTag(IdentityInfoActivity.class);
    @ViewById
    ImageView profileImageView;
    @ViewById
    TextView nickNameTextView;
    @ViewById
    TextView weiboTextView;
    @ViewById
    TextView genderTextView;
    @ViewById
    TextView areaTextView;

    @AfterViews
    void initViews() {
        AccountResult.Data data = AccountHelper.getIdentityInfo(this);
        Picasso.with(this).load(data.getAvatar_url()).into(profileImageView);
        nickNameTextView.setText(data.getNickname());
        weiboTextView.setText(data.getWeibo_uid());//TODO set text
        setGender(data.getGender());
        areaTextView.setText(data.getArea());
    }

    public static final int MALE = 0;
    public static final int FEMALE = 1;
    public static final int PRIVATE = 2;
    private final String[] gender = {"m", "f", "n"};
    private int genderSelected = MALE;

    private void setGender(int gender) {
        genderSelected = gender;
        switch (gender) {
            case MALE:
                genderTextView.setText(String.valueOf('\u2642'));
                break;
            case FEMALE:
                genderTextView.setText(String.valueOf('\u2640'));
                break;
            case PRIVATE:
                genderTextView.setText(getString(R.string.integrateInfoGenderPrivate));
            default:
                LogUtil.e(TAG, "unknown gender!!");
                break;
        }
    }

    private void setGender(String gender) {
        int genderInt = gender.indexOf(gender.trim());
        genderSelected = genderInt;
        setGender(genderInt);
    }

    @Click({R.id.profileItem, R.id.weiboItem, R.id.areaItem})
    void infoItem(View view) {
        switch (view.getId()) {
            case R.id.weiboItem:
                //TODO bind weibo
                break;

            default:
                //TODO put extras to let it preset

                break;
        }

    }

    @Click
    void nickNameItemClicked(View view) {
        final View customView = LayoutInflater.from(this).inflate(R.layout.dialog_nickname, null);
        final EditText edit = (EditText) customView.findViewById(R.id.nickNameEditText);
        final String oldName = nickNameTextView.getText().toString().trim();
        edit.setText(oldName);
        new MaterialDialog.Builder(this).theme(Theme.DARK).customView(customView, false)
                .title(R.string.integrateInfoNickNameHint)
                .positiveText(R.string.identityInfoOk)
                .negativeText(R.string.identityInfoCancel)
                .autoDismiss(false).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                String newName = edit.getText().toString().trim();
                if (!AccountHelper.isValidNickname(newName)) {
                    showNotification(R.string.identityInfoNicknameInvalid);
                    YoYo.with(Techniques.Shake).duration(700).playOn(edit);
                } else if (!oldName.equals(newName)) {
                    updateInfo("nickname", newName, nickNameTextView);
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                dialog.dismiss();
            }
        }).show();
    }

    @Click
    void genderItemClicked(View view) {
        new MaterialDialog.Builder(this)
                .theme(Theme.DARK)
                .title(R.string.integrateInfoGenderHint)
                .items(R.array.integrateInfoGenderArray)
                .itemsCallbackSingleChoice(genderSelected, (dialog, view1, which, text) -> {
                    updateGender(which);
                    return true; // allow selection
                }).positiveText(R.string.integrateInfoChooseBtn)
                .show();
    }

    @Background
    void updateGender(int which) {
        showProgress();
        ((IdentityInfo.Update) (new IdentityInfo.Update().with(this)))
                .setGender(gender[which]).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                showNotification(R.string.identityInfoUpdateFail);
            } else switch (result.getCode()) {
                case ErrorCode.SUCCESS:
                    AccountHelper.updateAccount(this, result.getData());
                    showNotification(R.string.identityInfoUpdateSuccess);
                    runOnUiThread(() -> setGender(which));
                    break;
                case ErrorCode.GENDER_FORMAT_ERROR:
                    LogUtil.e(TAG, "GENDER_FORMAT_ERROR");
                    break;
                default:
                    showNotification(R.string.identityInfoUpdateFail);
                    break;
            }
            hideProgress();
        });
    }

    @Background
    void updateInfo(String key, String value, TextView showValueView) {
        showProgress();
        ((IdentityInfo.Update) (new IdentityInfo.Update().with(this)))
                .set(key, value).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                showNotification(R.string.identityInfoUpdateFail);
            } else switch (result.getCode()) {
                case ErrorCode.SUCCESS:
                    AccountHelper.updateAccount(this, result.getData());
                    showNotification(R.string.identityInfoUpdateSuccess);
                    runOnUiThread(() -> showValueView.setText(value));
                    break;
                case ErrorCode.NICKNAME_EXISTS:
                    showNotification(R.string.identityInfoNicknameExist);
                    break;
                case ErrorCode.NICKNAME_FORMAT_ERROR:
                    showNotification(R.string.identityInfoNicknameNetworkInvalid);
                    break;
                case ErrorCode.LOCATION_FORMAT_ERROR:
                    LogUtil.e(TAG, "LOCATION_FORMAT_ERROR");
                    break;
                //TODO add more
                default:
                    showNotification(R.string.identityInfoUpdateFail);
                    break;
            }
            hideProgress();
        });
    }

    @OnActivityResult(REQUEST_UPDATE_INFO)
    void onUpdate(int resultCode) {
        if (resultCode == RESULT_OK) {
            initViews();
        }
    }

    @Click(R.id.logoutBtn)
    void logout(View view) {
        AccountHelper.deleteAccount(this);
        this.finish();
    }
}
