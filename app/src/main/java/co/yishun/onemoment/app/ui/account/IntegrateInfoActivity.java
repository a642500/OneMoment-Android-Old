package co.yishun.onemoment.app.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.request.sync.GetToken;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.qiniu.android.storage.UploadManager;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.StringArrayRes;

import java.io.File;

/**
 * Created by Carlos on 2015/4/2.
 */
@EActivity(R.layout.activity_integrate_info)
public class IntegrateInfoActivity extends ToolbarBaseActivity {
    public static final String TAG = LogUtil.makeTag(IntegrateInfoActivity.class);
    public static final int REQUEST_PHONE = 0;
    public static final int REQUEST_WEIBO = 1;

    //    public static final String EXTRA_SIGN_UP_TYPE = "type";
//    public static final String EXTRA_PHONE = "phone";

    //    public enum SignUpType {phone, weibo}
    public static final int MALE = 0;
    public static final int FEMALE = 1;
    public static final int PRIVATE = 2;
    private final String[] gender = {"m", "f", "n"};
    @ViewById
    EditText nickNameEditText;

    @ViewById
    TextView genderTextView;
    private int genderSelected = MALE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
    }

    @StringArrayRes
    String[] provinces;
    private String mProvince;
    private String mDistrict;
    @ViewById
    TextView areaTextView;

    @Click void areaItemClicked(View view) {
        MaterialDialog dialog = new MaterialDialog.Builder(this).theme(Theme.DARK).title(getString(R.string.integrateInfoAreaHint))
                .positiveText(R.string.integrateInfoChooseBtn).customView(R.layout.dialog_area_pick).build();
        View dialogView = dialog.getCustomView();
        Spinner provinceSpinner = (Spinner) dialogView.findViewById(R.id.provinceSpinner);
        Spinner districtSpinner = (Spinner) dialogView.findViewById(R.id.districtSpinner);

        districtSpinner.setEnabled(false);
        provinceSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, provinces));
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                districtSpinner.setEnabled(true);
                districtSpinner.setAdapter(new ArrayAdapter<>(IntegrateInfoActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(Constants.provincesItemsRes[position])));
                districtSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                districtSpinner.setEnabled(false);
            }
        });
        provinceSpinner.setSelection(0);
        dialog.setOnDismissListener(dialog1 -> {
            String province = (String) provinceSpinner.getSelectedItem();
            String district = (String) districtSpinner.getSelectedItem();
            setProvinceAndDistrict(province, district);
        });
        dialog.show();
    }

    @AfterViews void initAreaTextView() {
        setProvinceAndDistrict(provinces[0], getResources().getStringArray(Constants.provincesItemsRes[0])[0]);
    }

    private void setProvinceAndDistrict(String pro, String dis) {
        mProvince = pro;
        mDistrict = dis;
        areaTextView.setText(pro + " " + dis);
    }

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

    @Click void genderItemClicked(View view) {
        new MaterialDialog.Builder(this)
                .theme(Theme.DARK)
                .title(R.string.integrateInfoGenderHint)
                .items(R.array.integrateInfoGenderArray)
                .itemsCallbackSingleChoice(genderSelected % 2, (dialog, view1, which, text) -> {
                    setGender(which);
                    return true; // allow selection
                })
                .positiveText(R.string.integrateInfoChooseBtn)
                .show();
    }

    @UiThread void shakeNickNameEditText() {
        YoYo.with(Techniques.Shake).duration(getResources().getInteger(R.integer.defaultShakeDuration)).playOn(nickNameEditText);
    }

    @Click
    @Background void okBtnClicked(View view) {
        String nickname = String.valueOf(nickNameEditText.getText());
        if (TextUtils.isEmpty(nickname)) {
            shakeNickNameEditText();
            showNotification(R.string.integrateInfoNameEmpty);
            return;
        }

        showProgress();
        if (croppedProfileUri != null) {
            String uriString = croppedProfileUri.toString();
            String path = uriString.substring(uriString.indexOf(":") + 1);
            String qiNiuKey = Config.PROFILE_PREFIX + AccountHelper.getIdentityInfo(this).get_id() + Config.URL_HYPHEN + System.currentTimeMillis() + Config.PROFILE_SUFFIX;

            UploadManager uploadManager = new UploadManager();
            new GetToken().setFileName(qiNiuKey).with(this).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification(R.string.identityInfoUpdateFail);
                    hideProgress();
                } else if (result.getCode() != ErrorCode.SUCCESS) {
                    showNotification(R.string.identityInfoUpdateFail);
                    LogUtil.e(TAG, "get token failed: " + result.getCode());
                    hideProgress();
                } else {
                    uploadManager.put(path, qiNiuKey, result.getData().getToken(), (s, responseInfo, jsonObject) -> {
                                LogUtil.i(TAG, responseInfo.toString());
                                if (responseInfo.isOK()) {
                                    LogUtil.i(TAG, "profile upload ok");
                                    updateInfo(qiNiuKey, nickname);
                                } else {
                                    LogUtil.e(TAG, "profile upload error");
                                    showNotification(R.string.identityInfoUpdateFail);
                                    hideProgress();
                                }
                            }, null
                    );
                }
            });
        } else updateInfo(null, nickname);


    }

    @Background void updateInfo(@Nullable String qiNiuKey, String nickname) {
        IdentityInfo.Update bu = ((IdentityInfo.Update) (new IdentityInfo.Update().with(this))).setGender(gender[genderSelected]);
        if (qiNiuKey != null) bu = bu.setAvatarUrl(Config.getResourceUrl(this) + qiNiuKey);
        bu.setLocation(mProvince + " " + mDistrict).setNickname(nickname).setCallback(
                (e, result) -> {
                    if (e != null) {
                        e.printStackTrace();
                        showNotification(R.string.integrateInfoUpdateFail);
                    } else if (result.getCode() == ErrorCode.SUCCESS) {
                        AccountHelper.updateAccount(this, result.getData());
                        showNotification(R.string.integrateInfoUpdateSuccess);
                        setResult(RESULT_OK);
                        this.finish();
                    } else {
                        showNotification(R.string.integrateInfoUpdateFail);
                    }
                    hideProgress();
                });

    }

    @ViewById
    ImageView profileImageView;

    @Click void profileImageViewClicked(View view) {
        Crop.pickImage(this);
    }

    Uri croppedProfileUri;

    @OnActivityResult(Crop.REQUEST_PICK)
    @Background void onPictureSelected(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri selectedImage = data.getData();
                croppedProfileUri = Uri.fromFile(new File(getCacheDir(), "croppedProfile"));
                new Crop(selectedImage).output(croppedProfileUri).asSquare().start(this);
            } catch (Exception e) {
                e.printStackTrace();
                showNotification(R.string.identityInfoSelectProfileFail);
            }
        } else {
            LogUtil.i(TAG, "RESULT_CANCELED");
        }
    }

    @OnActivityResult(Crop.REQUEST_CROP)
    @Background void onPictureCropped(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            setProfileImage(croppedProfileUri);
        } else croppedProfileUri = null;
    }

    @UiThread void setProfileImage(Uri uri) {
//        if (needInvalidate) {
//            Picasso.with(this).invalidate(uri);
//            Picasso.with(this).load(uri).into(profileImageView);
//        } else {
//            needInvalidate = true;
//        }
        Picasso.with(this).load(uri).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(profileImageView);
    }
}
