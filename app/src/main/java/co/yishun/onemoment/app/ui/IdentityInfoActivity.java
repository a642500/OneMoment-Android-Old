package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.Bind;
import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.request.account.UnBind;
import co.yishun.onemoment.app.net.request.sync.GetToken;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import co.yishun.onemoment.app.util.WeiboHelper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.qiniu.android.storage.UploadManager;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.StringArrayRes;

import java.io.File;
import java.util.Arrays;

@EActivity(R.layout.activity_identity_info)
public class IdentityInfoActivity extends ToolbarBaseActivity {
    public static final int REQUEST_UPDATE_INFO = 100;
    public static final int REQUEST_IMAGE_SELECT = 101;
    public static final int REQUEST_IMAGE_CROP = 102;
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

    @AfterViews void initViews() {
        AccountResult.Data data = AccountHelper.getIdentityInfo(this);
        Picasso.with(this).load(data.getAvatar_url()).into(profileImageView);
        nickNameTextView.setText(data.getNickname());
        setWeiboUid(data.getWeibo_uid());
        setGender(data.getGender());
        setProvinceAndDistrict(data.getArea());
    }

    public static final int MALE = 0;
    public static final int FEMALE = 1;
    public static final int PRIVATE = 2;
    private final String[] genders = {"m", "f", "n"};
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
        int genderInt = 2;
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equals(gender.trim())) genderInt = i;
        }
        genderSelected = genderInt;
        setGender(genderInt);
    }

    @Click void profileItemClicked(View view) {
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
            showProgress();
            String uriString = croppedProfileUri.toString();
            uploadProfile(uriString.substring(uriString.indexOf(":") + 1));
        }
//        else showNotification(R.string.identityInfoSelectProfileFail);
    }

    @Background void uploadProfile(String path) {
        UploadManager uploadManager = new UploadManager();
        String qiNiuKey = Config.PROFILE_PREFIX + AccountHelper.getIdentityInfo(this).get_id() + Config.URL_HYPHEN + System.currentTimeMillis() + Config.PROFILE_SUFFIX;
        new GetToken().setFileName(qiNiuKey).with(this).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                showNotification(R.string.identityInfoUpdateFail);
            } else if (result.getCode() != ErrorCode.SUCCESS) {
                showNotification(R.string.identityInfoUpdateFail);
                LogUtil.e(TAG, "get token failed: " + result.getCode());
            } else {
                uploadManager.put(path, qiNiuKey, result.getData().getToken(),
                        (s, responseInfo, jsonObject) -> {
                            LogUtil.i(TAG, responseInfo.toString());
                            if (responseInfo.isOK()) {
                                LogUtil.i(TAG, "profile upload ok");
                                updateProfile(qiNiuKey);
                            } else {
                                LogUtil.e(TAG, "profile upload error");
                                showNotification(R.string.identityInfoUpdateFail);
                            }
                            hideProgress();
                        },
                        null
                );
            }
        });
    }

    @Background void updateProfile(String qiNiuKey) {
        new IdentityInfo.Update().setAvatarUrl(Config.getResourceUrl(this) + qiNiuKey)
                .with(this).setCallback((e1, result1) -> {
            if (e1 != null) {
                e1.printStackTrace();
            } else if (result1.getCode() != ErrorCode.SUCCESS) {
                LogUtil.e(TAG, "profile upload success bu update failed");
                showNotification(R.string.identityInfoUpdateFail);
            } else {
                AccountHelper.updateAccount(this, result1.getData());
                setProfileImage(croppedProfileUri);
                showNotification(R.string.identityInfoUpdateSuccess);
            }
        });
    }

    @UiThread void setProfileImage(Uri uri) {
        Picasso.with(this).load(uri).into(profileImageView);
    }

    @Click void nickNameItemClicked(View view) {
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

    @Click void genderItemClicked(View view) {
        new MaterialDialog.Builder(this)
                .theme(Theme.DARK)
                .title(R.string.integrateInfoGenderHint)
                .items(R.array.integrateInfoGenderArray)
                .itemsCallbackSingleChoice(genderSelected % 2, (dialog, view1, which, text) -> {
                    updateGender(which);
                    return true; // allow selection
                }).positiveText(R.string.integrateInfoChooseBtn)
                .show();
    }

    @Background void updateGender(int which) {
        showProgress();
        ((IdentityInfo.Update) (new IdentityInfo.Update().with(this)))
                .setGender(genders[which]).setCallback((e, result) -> {
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

    @StringArrayRes
    String[] provinces;
    private String mProvince;
    private String mDistrict;

    private void setProvinceAndDistrict(String pro, String dis) {
        mProvince = pro;
        mDistrict = dis;
        areaTextView.setText(pro + " " + dis);
    }

    private void setProvinceAndDistrict(String provinceAndDistrict) {
        if (provinceAndDistrict == null || provinceAndDistrict.length() < 2) {
            areaTextView.setText(provinceAndDistrict);
            return;
        }
        String twoChar = provinceAndDistrict.substring(0, 2);
        for (String province : provinces) {
            if (province.startsWith(twoChar)) mProvince = province;
        }
        mDistrict = provinceAndDistrict.substring(mProvince.length()).trim();
        areaTextView.setText(provinceAndDistrict);
    }


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
                String[] districts = getResources().getStringArray(Constants.provincesItemsRes[position]);
                districtSpinner.setEnabled(true);
                districtSpinner.setAdapter(new ArrayAdapter<>(IdentityInfoActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, districts));
                int selected = Arrays.asList(districts).indexOf(mDistrict);
                districtSpinner.setSelection(selected >= 0 ? selected : 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                districtSpinner.setEnabled(false);
            }
        });
        int selected = Arrays.asList(provinces).indexOf(mProvince);
        provinceSpinner.setSelection(selected >= 0 ? selected : 0);
        dialog.setOnDismissListener(dialog1 -> {
            String province = (String) provinceSpinner.getSelectedItem();
            String district = (String) districtSpinner.getSelectedItem();
            if (!province.equals(mProvince) || !district.equals(mDistrict))
                updateArea(province, district);
        });
        dialog.show();
    }

    @Background void updateArea(@NonNull String pro, @NonNull String dis) {
        showProgress();
        ((IdentityInfo.Update) (new IdentityInfo.Update().with(this)))
                .setLocation(pro + " " + dis).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                showNotification(R.string.identityInfoUpdateFail);
            } else switch (result.getCode()) {
                case ErrorCode.SUCCESS:
                    AccountHelper.updateAccount(this, result.getData());
                    showNotification(R.string.identityInfoUpdateSuccess);
                    runOnUiThread(() -> setProvinceAndDistrict(pro, dis));
                    break;
                case ErrorCode.LOCATION_FORMAT_ERROR:
                    LogUtil.e(TAG, "LOCATION_FORMAT_ERROR");
                    break;
                default:
                    showNotification(R.string.identityInfoUpdateFail);
                    break;
            }
            hideProgress();
        });
    }

    @Background void updateInfo(String key, String value, TextView showValueView) {
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

    @Click void weiboItemClicked(View view) {
        if (AccountHelper.getIdentityInfo(this).getPhone() == null) {
            showNotification(R.string.identityInfoWeiboUnboundImpossible);
            return;
        }
        if (uid != null) {
            new MaterialDialog.Builder(this).title(R.string.identityInfoWeiboUnboundDialogTitle).content(R.string.identityInfoWeiboUnboundDialogContent).negativeText(R.string.identityInfoCancel).positiveText(R.string.identityInfoOk).theme(Theme.DARK).callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    unbind();
                }
            }).show();
        } else {
            final WeiboHelper helper = new WeiboHelper(this);
            helper.login(new WeiboHelper.WeiboLoginListener() {
                @Override
                public void onSuccess(Oauth2AccessToken token) {
                    bind(token);
                    showNotification(R.string.weiboBindAuthSuccess);
                }

                @Override
                public void onFail() {
                    showNotification(R.string.weiboBindFail);
                }

                @Override
                public void onCancel() {
                    showNotification(R.string.weiboBindCancel);
                }
            });
        }
    }

    @Background void bind(Oauth2AccessToken token) {
        //weibo user won't run this code, don't need take in account
        showProgress();
        new Bind.Weibo().setUid(token.getUid()).with(this).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
            } else if (result.getCode() == ErrorCode.SUCCESS) {
                AccountHelper.updateAccount(this, result.getData());
                setWeiboUid(token.getUid());
                showNotification(R.string.identityInfoWeiboBoundSuccess);
            } else {
                showNotification(R.string.identityInfoWeiboBoundFail);
            }
            hideProgress();
        });

    }


    @Background void unbind() {
        showProgress();
        new UnBind.Weibo().setUid(uid).with(this).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
            } else if (result.getCode() == ErrorCode.SUCCESS) {
                AccountHelper.updateAccount(this, result.getData());
                setWeiboUid(null);
                showNotification(R.string.identityInfoWeiboUnboundSuccess);
            } else {
                showNotification(R.string.identityInfoWeiboUnboundFail);
            }
            hideProgress();
        });
    }

    /**
     * set user uid, null to unbound.This will update ui but won't save data.
     *
     * @param uid
     */
    @UiThread void setWeiboUid(@Nullable String uid) {
        this.uid = uid;
        weiboTextView.setText(uid == null ? R.string.identityInfoWeiboUnbound : R.string.identityInfoWeiboBound);
    }

    private String uid = null;

    @Click(R.id.logoutBtn) void logout(View view) {
        new MaterialDialog.Builder(this).theme(Theme.DARK).title(R.string.identityInfoLogout).content(R.string.identityInfoLogoutAlert)
                .positiveText(R.string.identityInfoLogout).negativeText(R.string.identityInfoCancel).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
            }

            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                deleteAccountBackground();
                IdentityInfoActivity.this.finish();
            }
        }).show();
    }

    @Background void deleteAccountBackground() {
        AccountHelper.deleteAccount(this);
    }
}
