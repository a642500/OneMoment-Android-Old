package co.yishun.onemoment.app.ui.guide;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import co.yishun.onemoment.app.R;
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

    private OnLastBtnClickedListener mListener;

    // TODO: Rename method, update argument and hook method into UI event

    @Click
    public void okBtnClicked(View view) {
        if (mListener != null) {
            mListener.onLastBtnClicked(view);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isLast)
            try {
                mListener = (OnLastBtnClickedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnLastBtnClickedListener");
            }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLastBtnClickedListener {
        void onLastBtnClicked(View view);
    }

}
