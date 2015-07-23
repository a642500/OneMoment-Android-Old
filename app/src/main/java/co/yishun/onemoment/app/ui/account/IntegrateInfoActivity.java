package co.yishun.onemoment.app.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.config.ErrorCode;
import co.yishun.onemoment.app.net.request.account.CheckNickName;
import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.request.account.SignUp;
import co.yishun.onemoment.app.net.request.sync.GetToken;
import co.yishun.onemoment.app.ui.ToolbarBaseActivity;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.StringArrayRes;

import java.io.File;
import java.util.concurrent.CountDownLatch;

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
    private final String[] genders = {"m", "f", "n"};
    @ViewById
    EditText nickNameEditText;
    @ViewById
    TextView genderTextView;
    @StringArrayRes
    String[] provinces;
    @ViewById
    TextView areaTextView;
    @ViewById
    ImageView profileImageView;
    Uri croppedProfileUri;
    //    @Extra Bundle weiboToken;
    @Extra
    String access_token;
    @Extra
    String refresh_token;
    @Extra
    String expires_in;

    //    @Extra Bundle weiboToken;// from weibo login, weibo user need to check identity info
    @Extra
    String uid;
    private int genderSelected = MALE;
    private String mProvince;
    private String mDistrict;
    private Oauth2AccessToken mWeiboToken;
    private MaterialDialog mGetInfoProgressDialog;
    /**
     * only for weibo login to store weibo avatar url.
     */
    private String avatarUrl;
    private boolean isNickNameOk = false;
    /**
     * Used by {@link #uploadProfileImage(String)} to sign whether upload success
     */
    private boolean isUploadSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
    }

    @Click
    void areaItemClicked(View view) {
        MaterialDialog dialog = new MaterialDialog.Builder(this).theme(Theme.DARK).title(getString(R.string.integrateInfoAreaHint))
                .positiveText(R.string.integrateInfoChooseBtn).customView(R.layout.dialog_area_pick, false).build();
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

    @AfterExtras
    void initWeiboInfo() {
        if (access_token != null) {
            mWeiboToken = new Oauth2AccessToken();
            mWeiboToken.setExpiresTime(Long.parseLong(expires_in));
            mWeiboToken.setToken(access_token);
            mWeiboToken.setUid(uid);
            mWeiboToken.setRefreshToken(refresh_token);

            loadWeiboInfo();
        }
    }

    @UiThread
    void showGetInfoDialog() {
        mGetInfoProgressDialog = new MaterialDialog.Builder(this).theme(Theme.DARK).progress(true, 0).content(R.string.integrateInfoGetWeiboInfo).build();
        mGetInfoProgressDialog.show();
        mGetInfoProgressDialog.setOnCancelListener(dialog -> this.finish());// finish if press back
    }

    @UiThread
    void hideGetInfoDialog() {
        if (mGetInfoProgressDialog != null) {
            mGetInfoProgressDialog.dismiss();
        }
    }

    @Background
    void loadWeiboInfo() {
        WeiboHelper weiboHelper = new WeiboHelper(this);
        LogUtil.i(TAG, mWeiboToken.toString());

        showGetInfoDialog();


        WeiboHelper.WeiBoInfo info = weiboHelper.getUserInfo(mWeiboToken);
        if (info == null || info.name == null) {
            showNotification(R.string.integrateInfoGetWeiboInfoFail);
        } else {
            showNotification(R.string.integrateInfoGetWeiboInfoSuccess);
            Log.i(TAG, info.toString());
            showWeiboInfo(info);
            // fill weibo info

        }

        hideGetInfoDialog();
    }

    @UiThread
    void showWeiboInfo(WeiboHelper.WeiBoInfo info) {
        Picasso.with(this).load(info.avatar_large).into(profileImageView);
        avatarUrl = info.avatar_large;
        nickNameEditText.setText(info.name);
        setGender(info.gender);
        setProvinceAndDistrict(info.location);
    }

    @Background
    void signUpByWeibo(@Nullable String profileKey, String nickname, String gender, String location) {
        SignUp.ByWeiBo bw = new SignUp.ByWeiBo().setUid(mWeiboToken.getUid())
                .setGender(gender).setLocation(location)
                .setNickname(nickname);
        if (profileKey != null)
            bw.setAvatarUrl(Config.getResourceUrl(this) + profileKey);
        else
            bw.setAvatarUrl(avatarUrl).with(this).setCallback((e, result) -> {
                if (e != null) {
                    e.printStackTrace();
                    showNotification(R.string.weiboLoginSignUpFail);
                    hideProgress();
                } else if (result.getCode() == ErrorCode.SUCCESS) {
                    AccountHelper.createAccount(this, result.getData());
                    showNotification(R.string.weiboLoginSignUpSuccess);
                    postFinish();
                    hideProgress();
                }
            });
    }

    @AfterViews
    void initAreaTextView() {
        setProvinceAndDistrict(provinces[0], getResources().getStringArray(Constants.provincesItemsRes[0])[0]);
    }

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
        if (gender == null) gender = "m";
        int genderInt = 2;
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equals(gender.trim())) genderInt = i;
        }
        genderSelected = genderInt;
        setGender(genderInt);
    }

    @Click
    void genderItemClicked(View view) {
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

    @UiThread
    void shakeNickNameEditText() {
        YoYo.with(Techniques.Shake).duration(getResources().getInteger(R.integer.defaultShakeDuration)).playOn(nickNameEditText);
    }

    @Click
    @Background
    void okBtnClicked(View view) {
        String nickname = String.valueOf(nickNameEditText.getText());
        if (TextUtils.isEmpty(nickname)) {
            shakeNickNameEditText();
            showNotification(R.string.integrateInfoNameEmpty);
            return;
        }
        showProgress();
        if (!checkNickname(nickname)) {
            showNotification(R.string.integrateInfoNameNotUnique);
            hideProgress();
            return;
        }

        String profileKey = null;
        if (croppedProfileUri != null) {
            String uriString = croppedProfileUri.toString();
            String path = uriString.substring(uriString.indexOf(":") + 1);
            profileKey = uploadProfileImage(path);
        }

        if (mWeiboToken == null) {
            updateInfo(profileKey, nickname, genders[genderSelected], mProvince + " " + mDistrict);
        } else {
            signUpByWeibo(profileKey, nickname, genders[genderSelected], mProvince + " " + mDistrict);
        }


    }

    private boolean checkNickname(String name) {
        CountDownLatch latch = new CountDownLatch(1);
        new CheckNickName().setNickname(name).with(this).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                isNickNameOk = false;
            } else if (result.getCode() == ErrorCode.SUCCESS) {
                isNickNameOk = true;
            } else {
                isNickNameOk = false;
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            isNickNameOk = false;
        }
        return isNickNameOk;
    }

    /**
     * upload profile image to server
     *
     * @param path to image
     * @return file key of the file on server, null if upload failed
     */
    private String uploadProfileImage(@NonNull String path) {
        // if upload failed, set this value null
        String qiNiuKey = Config.PROFILE_PREFIX + AccountHelper.getIdentityInfo(this).get_id() + Config.URL_HYPHEN + (System.currentTimeMillis() / 1000) + Config.PROFILE_SUFFIX;

        UploadManager uploadManager = new UploadManager();
        CountDownLatch latch = new CountDownLatch(1);

        new GetToken().setFileName(qiNiuKey).with(this).setCallback((e, result) -> {
            if (e != null) {
                e.printStackTrace();
                isUploadSuccess = false;
            } else if (result.getCode() != ErrorCode.SUCCESS) {
                LogUtil.e(TAG, "get token failed: " + result.getCode());
                isUploadSuccess = false;
            } else {
                uploadManager.put(path, qiNiuKey, result.getData().getToken(), (s, responseInfo, jsonObject) -> {
                            LogUtil.i(TAG, responseInfo.toString());
                            if (responseInfo.isOK()) {
                                LogUtil.i(TAG, "profile upload ok");
                                isUploadSuccess = true;
                            } else {
                                LogUtil.e(TAG, "profile upload error");
                                isUploadSuccess = false;
                            }
                        }, null
                );
            }
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isUploadSuccess ? qiNiuKey : null;
    }

    @Background
    void updateInfo(@Nullable String qiNiuKey, String nickname, String gender, String location) {
        showProgress();
        IdentityInfo.Update bu = ((IdentityInfo.Update) (new IdentityInfo.Update().with(this))).setGender(gender);
        if (qiNiuKey != null) bu = bu.setAvatarUrl(Config.getResourceUrl(this) + qiNiuKey);
        bu.setLocation(location).setNickname(nickname).setCallback(
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

    @Click
    void profileImageViewClicked(View view) {
        Crop.pickImage(this);
    }

    @OnActivityResult(Crop.REQUEST_PICK)
    @Background
    void onPictureSelected(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri selectedImage = data.getData();
                croppedProfileUri = Uri.fromFile(new File(getCacheDir(), "croppedProfile"));
                Crop.of(selectedImage, croppedProfileUri).asSquare().start(this);
            } catch (Exception e) {
                e.printStackTrace();
                showNotification(R.string.identityInfoSelectProfileFail);
            }
        } else {
            LogUtil.i(TAG, "RESULT_CANCELED");
        }
    }

    @OnActivityResult(Crop.REQUEST_CROP)
    @Background
    void onPictureCropped(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            setProfileImage(croppedProfileUri);
        } else croppedProfileUri = null;
    }

    @UiThread
    void setProfileImage(Uri uri) {
//        if (needInvalidate) {
//            Picasso.with(this).invalidate(uri);
//            Picasso.with(this).load(uri).into(profileImageView);
//        } else {
//            needInvalidate = true;
//        }
        Picasso.with(this).load(uri).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(profileImageView);
    }

    @UiThread(delay = 300)
    void postFinish() {
        this.finish();
    }
}
