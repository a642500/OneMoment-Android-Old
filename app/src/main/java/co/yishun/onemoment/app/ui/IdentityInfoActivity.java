package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.net.request.account.IdentityInfo;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_identity_info)
public class IdentityInfoActivity extends ToolbarBaseActivity {
    private static final String TAG = LogUtil.makeTag(IdentityInfoActivity.class);
    public static final int REQUEST_UPDATE_INFO = 0;

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

    private void setGender(String gender) {
        if (gender.startsWith("m"))
            genderTextView.setText(String.valueOf('\u2642'));
        else if (gender.startsWith("f"))
            genderTextView.setText(String.valueOf('\u2640'));
        else
            genderTextView.setText(getString(R.string.integrateInfoGenderPrivate));
    }

    @Click({R.id.profileItem, R.id.nickNameItem, R.id.weiboItem, R.id.genderItem, R.id.areaItem})
    void infoItem(View view) {
        Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
        switch (view.getId()) {
            case R.id.weiboItem:
                //TODO bind weibo
                break;
            default:
                //TODO put extras to let it preset
                IdentityInfoActivity_.intent(this).startForResult(REQUEST_UPDATE_INFO);
                break;
        }
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
