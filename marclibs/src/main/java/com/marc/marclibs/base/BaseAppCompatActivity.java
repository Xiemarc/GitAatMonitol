package com.marc.marclibs.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.marc.marclibs.R;
import com.marc.marclibs.loading.VaryViewHelperController;
import com.marc.marclibs.netstatus.NetChangeObserver;
import com.marc.marclibs.netstatus.NetStateReceiver;
import com.marc.marclibs.netstatus.NetUtils;
import com.marc.marclibs.utils.CommonUtils;
import com.marc.marclibs.utils.StatusBarCompat;

import butterknife.ButterKnife;

/**
 * 描述：Base的activity基类
 * 作者：Marc on 2016/8/12 16:57
 * 邮箱：aliali_ha@yeah.net
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    /**
     * 日志信息
     */
    protected static String TAG_LOG = null;

    /**
     * 屏幕信息
     */
    protected int mScreenWidth = 0;
    protected int mScreenHeight = 0;
    protected float mScreenDensity = 0.0f;

    /**
     * 上下文
     */
    protected Context mContext = null;

    /**
     * 网络状态
     */
    protected NetChangeObserver mNetChangeObserver = null;

    /**
     * 加载中视图控制器
     */
    private VaryViewHelperController mVaryViewHelperController = null;

    /**
     * activity切换模式
     */
    public enum TransitionMode {
        LEFT, RIGHT, TOP, BOTTOM, SCALE, FADE
    }

    /**
     * 状态栏颜色
     */
    protected int statusBarColor = 0;
    /**
     * 状态栏view
     */
    protected View statusBarView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (toggleOverridePendingTransition()) { //如果需要切换acitivty
            switch (getOverridePendingTransitionMode()) {//得到当前用户设定的切换模式
                case LEFT:
                    overridePendingTransition(R.anim.gallery_left_in, R.anim.gallery_left_out);
                    break;
                case RIGHT:
                    overridePendingTransition(R.anim.gallery_right_in, R.anim.gallery_right_out);
                    break;
                case TOP:
                    overridePendingTransition(R.anim.gallery_top_in, R.anim.gallery_top_out);
                    break;
                case BOTTOM:
                    overridePendingTransition(R.anim.gallery_bottom_in, R.anim.gallery_bottom_out);
                    break;
                case SCALE:
                    overridePendingTransition(R.anim.gallery_scale_in, R.anim.gallery_scale_out);
                    break;
                case FADE:
                    overridePendingTransition(R.anim.gallery_fade_in, R.anim.gallery_fade_out);
                    break;
            }
        }
        super.onCreate(savedInstanceState);
        //基本设置
        // base setup
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            getBundleExtras(extras);
        }
        TAG_LOG = this.getClass().getSimpleName();
        BaseAppManager.getInstance().addActivity(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.density;
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;
        //加载布局
        if (getContentViewLayoutID() != 0) {
            setContentView(getContentViewLayoutID());
        } else {
            throw new IllegalArgumentException("你必须返回一个正确的布局ID");
        }
        if (statusBarColor == 0) {
            statusBarView = StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.primary_light));
        } else if (statusBarColor != -1) {
            statusBarView = StatusBarCompat.compat(this, statusBarColor);
        }
        transparent19and20();
        mContext = this;
        mNetChangeObserver = new NetChangeObserver() {
            @Override
            public void onNetConnected(NetUtils.NetType type) {
                super.onNetConnected(type);
                onNetworkConnected(type);
            }

            @Override
            public void onNetDisConnect() {
                super.onNetDisConnect();
                onNetworkDisConnected();
            }
        };
        NetStateReceiver.registerObserver(mNetChangeObserver);
        initViewsAndEvents();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        if (null != getLoadingTargetView()) {
            mVaryViewHelperController = new VaryViewHelperController(getLoadingTargetView());
        }
    }

    protected void transparent19and20() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    @Override
    public void finish() {
        super.finish();
        BaseAppManager.getInstance().removeActivity(this);
        if (toggleOverridePendingTransition()) {
            switch (getOverridePendingTransitionMode()) {
                case LEFT:
                    overridePendingTransition(R.anim.gallery_left_in, R.anim.gallery_left_out);
                    break;
                case RIGHT:
                    overridePendingTransition(R.anim.gallery_right_in, R.anim.gallery_right_out);
                    break;
                case TOP:
                    overridePendingTransition(R.anim.gallery_top_in, R.anim.gallery_top_out);
                    break;
                case BOTTOM:
                    overridePendingTransition(R.anim.gallery_bottom_in, R.anim.gallery_bottom_out);
                    break;
                case SCALE:
                    overridePendingTransition(R.anim.gallery_scale_in, R.anim.gallery_scale_out);
                    break;
                case FADE:
                    overridePendingTransition(R.anim.gallery_fade_in, R.anim.gallery_fade_out);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        NetStateReceiver.removeRegisterObserver(mNetChangeObserver);
        //解除eventbus的绑定
//        if (isBindEventBusHere()) {
//            EventBus.getDefault().unregister(this);
//        }
    }


    /**
     * 得到bundle数据
     *
     * @param extras
     */
    protected abstract void getBundleExtras(Bundle extras);

    /**
     * 加载布局
     * <br> 在setcontentview中的参数
     * * @return 返回布局id
     */
    protected abstract int getContentViewLayoutID();

    /**
     * 得到当前视图view
     */
    protected abstract View getLoadingTargetView();

    /**
     * 初始化所有的view和事件 在onCreate方法中执行
     */
    protected abstract void initViewsAndEvents();


    /**
     * 当事件到达
     *
     * @param eventCenter
     */
//    protected abstract void onEventComming(EventCenter eventCenter);

    /**
     * 网络连接
     */
    protected abstract void onNetworkConnected(NetUtils.NetType type);


    /**
     * 网络断开
     */
    protected abstract void onNetworkDisConnected();


//    /**
//     * 是否设置状态栏透明
//     * <br>状态栏的高度
//     *
//     * @return
//     */
//    protected abstract boolean isApplyStatusBarTranslucency();


    /**
     * 控制 activity切换模式
     *
     * @return
     */
    protected abstract boolean toggleOverridePendingTransition();

    /**
     * 得到用户定义的activity切换模式
     */
    protected abstract TransitionMode getOverridePendingTransitionMode();


    /**
     * 启动activity
     *
     * @param clazz
     */
    protected void readyGo(String action, Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        intent.setAction(action);
        startActivity(intent);
    }

    /**
     * 启动带bundle的activity
     *
     * @param clazz
     * @param bundle
     */
    protected void readyGo(String action, Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.setAction(action);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 启动activiu然后关闭
     *
     * @param clazz
     */
    protected void readyGoThenKill(String action, Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        intent.setAction(action);
        startActivity(intent);
        finish();
    }

    /**
     * 启动带bundel的activity然后关闭
     *
     * @param clazz
     * @param bundle
     */
    protected void readyGoThenKill(String action, Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.setAction(action);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        finish();
    }

    /**
     * startActivityForResult
     *
     * @param clazz
     * @param requestCode
     */
    protected void readyGoForResult(String action, Class<?> clazz, int requestCode) {
        Intent intent = new Intent(this, clazz);
        intent.setAction(action);
        startActivityForResult(intent, requestCode);
    }

    /**
     * startActivityForResult with bundle
     *
     * @param clazz
     * @param requestCode
     * @param bundle
     */
    protected void readyGoForResult(String action, Class<?> clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.setAction(action);
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
        //防止遮盖虚拟按键
        if (null != msg && !CommonUtils.isEmpty(msg)) {
            Snackbar.make(getLoadingTargetView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }


    /**
     * 是否显示loading
     * toggle show loading
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
     * 设置是否开启 显示空视图
     * toggle show empty
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
     * 显示 错误页面
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
     * 设置显示 网络错误视图
     * toggle show network error
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

    /**
     * 隐藏状态栏
     */
    protected void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
        if (statusBarView != null) {
            statusBarView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * 显示状态栏
     */
    protected void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
        if (statusBarView != null) {
            statusBarView.setBackgroundColor(statusBarColor);
        }
    }


}
