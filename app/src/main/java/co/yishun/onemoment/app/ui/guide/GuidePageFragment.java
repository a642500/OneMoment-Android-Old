package co.yishun.onemoment.app.ui.guide;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Config;
import co.yishun.onemoment.app.ui.RecordingActivity_;
import org.androidannotations.annotations.*;

@EFragment(R.layout.fragment_guide_page)
public class GuidePageFragment extends Fragment {

    @FragmentArg
    int imageRes;
    @FragmentArg
    boolean isLast;

    @ViewById
    ImageView guideImageView;
    @ViewById
    Button okBtn;

    @AfterViews
    void initViews() {
        guideImageView.setImageResource(imageRes);
        okBtn.setVisibility(isLast ? View.VISIBLE : View.GONE);
    }

    private Activity mActivity;

    @Click
    public void okBtnClicked(View view) {
        if (mActivity != null) {
            mActivity.getSharedPreferences(Config.PREFERENCE, Activity.MODE_PRIVATE).edit().putBoolean(Config.PREFERENCE_IS_FIRST_LAUNCH, false).apply();
            mActivity.finish();
            RecordingActivity_.intent(this).start();
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }


}
