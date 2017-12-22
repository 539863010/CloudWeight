package xm.cloudweight.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author wyh
 * @Description:
 * @creat 2017/12/3
 */
public abstract class BaseFragment extends Fragment {

    public View mView;
    public FragmentActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(getLayoutId(), container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = getActivity();
        getData();
        initView();
        loadData();

    }

    protected abstract int getLayoutId();

    protected abstract void getData();

    protected abstract void initView();

    protected abstract void loadData();


}
