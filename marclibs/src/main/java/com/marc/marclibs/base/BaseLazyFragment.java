package com.marc.marclibs.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marc.marclibs.loading.VaryViewHelperController;
import com.marc.marclibs.utils.CommonUtils;

import java.lang.reflect.Field;

import butterknife.ButterKnife;

/**
 * Description:fragment基类 解决预加载问题<br>
 * 这里没有加 网络监听判断，因为activity、已经加了，framgent在activity、里面
 * Created by Administrator on 2016/8/12.
 */

public abstract class BaseLazyFragment extends Fragment {

    /**
     * Log tag
     */
    protected static String TAG_LOG = null;

    /**
     * 屏幕信息
     */
    protected int mScreenWidth = 0;
    protected int mScreenHeight = 0;
    protected float mScreenDensity = 0.0f;

    /**
     * context
     */
    protected Context mContext = null;

    private boolean isFirstResume = true;
    private boolean isFirstVisible = true;
    private boolean isFirstInvisible = true;
    private boolean isPrepared;


    private VaryViewHelperController mVaryViewHelperController = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG_LOG = this.getClass().getSimpleName();
//        if (isBindEventBusHere()) {
//            EventBus.getDefault().register(this);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContentViewLayoutID() != 0) {
            return inflater.inflate(getContentViewLayoutID(), null);
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (null != getLoadingTargetView()) {
            mVaryViewHelperController = new VaryViewHelperController(getLoadingTargetView());
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mScreenDensity = displayMetrics.density;
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;

        initViewsAndEvents();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPrepare();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            return;
        }
        if (getUserVisibleHint()) {
            onUserVisible();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onUserInvisible();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (isBindEventBusHere()) {
//            EventBus.getDefault().unregister(this);
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // for bug ---> java.lang.IllegalStateException: Activity has been destroyed
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isFirstVisible) {
                isFirstVisible = false;
                initPrepare();
            } else {
                onUserVisible();
            }
        } else {
            if (isFirstInvisible) {
                isFirstInvisible = false;
                onFirstUserInvisible();
            } else {
                onUserInvisible();
            }
        }
    }

    private synchronized void initPrepare() {
        if (isPrepared) {
            onFirstUserVisible();
        } else {
            isPrepared = true;
        }
    }


    /**
     * 绑定xml布局ID
     *
     * @return id of layout resource
     */
    protected abstract int getContentViewLayoutID();


//    /**
//     *  当event事件到达
//     *
//     * @param eventCenter
//     */
//    protected abstract void onEventComming(EventCenter eventCenter);
//
//    /**
//     * 绑定eventbus
//     *
//     * @return
//     */
//    protected abstract boolean isBindEventBusHere();





    /**
     * 得到当前loading目标view
     */
    protected abstract View getLoadingTargetView();

    /**
     * 所有view的初始化以及事件 <br> 在onViewCreated中执行
     */
    protected abstract void initViewsAndEvents();


    /**
     * 当fragment是第一次可见,我们可以做一些初始化的工作或刷新数据只有一次<br>
     * when fragment is visible for the first time, here we can do some initialized work or refresh data only once
     */
    protected abstract void onFirstUserVisible();


    /**
     * 这个方法就像生命周期的 onResume（）方法<br>
     * this method like the fragment's lifecycle method onResume()
     */
    protected abstract void onUserVisible();

    /**
     * 当fragment 是 第一次不可见
     * <br>when fragment is invisible for the first time
     */
    private void onFirstUserInvisible() {
        // here we do not recommend do something
        //我们不建议做一些操作
    }

    /**
     * 这个方法相当于fragment生命周期的 onPause（）方法
     * this method like the fragment's lifecycle method onPause()
     */
    protected abstract void onUserInvisible();

    /**
     * 得到fragmentmanager
     *
     * @return
     */
    protected FragmentManager getSupportFragmentManager() {
        return getActivity().getSupportFragmentManager();
    }

    /**
     * startActivity
     *
     * @param clazz
     */
    protected void readyGo(Class<?> clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
    }


    /**
     * startActivity with bundle
     *
     * @param clazz
     * @param bundle
     */
    protected void readyGo(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(getActivity(), clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }


    /**
     * startActivityForResult
     *
     * @param clazz
     * @param requestCode
     */
    protected void readyGoForResult(Class<?> clazz, int requestCode) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivityForResult(intent, requestCode);
    }


    /**
     * startActivityForResult with bundle
     *
     * @param clazz
     * @param requestCode
     * @param bundle
     */
    protected void readyGoForResult(Class<?> clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(getActivity(), clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * show toast
     *
     * @param msg
     */
    protected void showToast(String msg) {
        if (null != msg && !CommonUtils.isEmpty(msg)) {
            Snackbar.make(((Activity) mContext).getWindow().getDecorView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置是否显示 loading的view
     *
     * @param toggle
     */
    protected void toggleShowLoading(boolean toggle, String msg) {
        if (null == mVaryViewHelperController) {
            throw new IllegalArgumentException("你必须返回一个正确的目标视图加载");
        }

        if (toggle) {
            mVaryViewHelperController.showLoading(msg);
        } else {
            mVaryViewHelperController.restore();
        }
    }

    /**
     * 设置显示 空视图
     *
     * @param toggle
     */
    protected void toggleShowEmpty(boolean toggle, String msg, View.OnClickListener onClickListener) {
        if (null == mVaryViewHelperController) {
            throw new IllegalArgumentException("你必须返回一个正确的目标视图加载");
        }

        if (toggle) {
            mVaryViewHelperController.showEmpty(msg, onClickListener);
        } else {
            mVaryViewHelperController.restore();
        }
    }


    /**
     * 设置是否显示 错误视图
     *
     * @param toggle
     */
    protected void toggleShowError(boolean toggle, String msg, View.OnClickListener onClickListener) {
        if (null == mVaryViewHelperController) {
            throw new IllegalArgumentException("你必须返回一个正确的目标视图加载");
        }

        if (toggle) {
            mVaryViewHelperController.showError(msg, onClickListener);
        } else {
            mVaryViewHelperController.restore();
        }
    }

    /**
     * 设置是否显示网络错误视图
     *
     * @param toggle
     */
    protected void toggleNetworkError(boolean toggle, View.OnClickListener onClickListener) {
        if (null == mVaryViewHelperController) {
            throw new IllegalArgumentException("你必须返回一个正确的目标视图加载");
        }

        if (toggle) {
            mVaryViewHelperController.showNetworkError(onClickListener);
        } else {
            mVaryViewHelperController.restore();
        }
    }


//    public void onEventMainThread(EventCenter eventCenter) {
//        if (null != eventCenter) {
//            onEventComming(eventCenter);
//        }
//    }
}
