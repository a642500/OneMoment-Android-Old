package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.net.result.AccountResult;
import co.yishun.onemoment.app.util.AccountHelper;
import co.yishun.onemoment.app.util.LogUtil;
import com.squareup.picasso.Picasso;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_identity_info)
public class IdentityInfoActivity extends ActionBarActivity {
    private static final String TAG = LogUtil.makeTag(IdentityInfoActivity.class);

    @ViewById
    Toolbar toolbar;

    @AfterViews
    void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(getString(R.string.identityInfoTitle));
        toolbar.setNavigationOnClickListener(v -> IdentityInfoActivity.this.onBackPressed());
    }

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
        weiboTextView.setText(data.getWeibo_uid());
    }

    @Click({R.id.profileItem, R.id.nickNameItem, R.id.weiboItem, R.id.genderItem, R.id.areaItem, R.id.logoutBtn})
    void onItemClick(View view) {
        switch (view.getId()) {
            case R.id.profileItem:

                break;
            case R.id.nickNameItem:
                break;
            case R.id.weiboItem:
                break;
            case R.id.genderItem:
                break;
            case R.id.areaItem:
                break;
            case R.id.logoutBtn:
                break;
            default:
                break;
        }
    }

    @Click(R.id.logoutBtn)
    void logout(View view) {
        AccountHelper.deleteAccount(this);
        AccountHelper.deleteIdentityInfo(this);
        this.finish();
    }
}
