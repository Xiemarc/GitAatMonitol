package com.marc.marclibs.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;

import com.marc.marclibs.swipeback.SwipeBackActivityBase;
import com.marc.marclibs.swipeback.SwipeBackActivityHelper;
import com.marc.marclibs.swipeback.SwipeBackLayout;
import com.marc.marclibs.swipeback.Utils;

/**
 * 描述：滑动退出的backcompatactivity
 * 作者：Marc on 2016/9/6 15:03
 * 邮箱：aliali_ha@yeah.net
 */
public abstract class BaseSwipeBackCompatActivity extends BaseAppCompatActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Nullable
    @Override
    public View findViewById(@IdRes int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
