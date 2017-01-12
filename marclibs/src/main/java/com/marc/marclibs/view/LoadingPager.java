package com.marc.marclibs.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.marc.marclibs.R;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * des：自定义实现loading加载。这里另外的实现
 * author：Xie
 */
public abstract class LoadingPager extends FrameLayout {

    public static final int STATE_NONE = -1;
    public static final int STATE_LODING = 0;
    public static final int STATE_EMPTY = 1;
    public static final int STATE_ERROR = 2;
    public static final int STATE_SUCCESS = 3;
    public static final int STATE_OFFNET = 4;

    public int mCurState = STATE_NONE;
    private View mLoadingView;//加载布局
    private View mErrorView;//错误布局
    private View mEmptyView;//空布局
    private View mOffNetView;//网络断开布局
    private View mSuccessView;//视图成功布局

    private Context context;

    private ImageView mImageView;
    private AnimationDrawable background;

    public LoadingPager(Context context) {
        super(context);
        this.context = context;
        initCommonView();
    }

    private void initCommonView() {
        mLoadingView = View.inflate(context, R.layout.sayhi_pager_loading, null);
        mImageView = (ImageView) mLoadingView.findViewById(R.id.loadingIv);
        background = (AnimationDrawable) mImageView.getBackground();
        background.start();
        this.addView(mLoadingView);
        mErrorView = View.inflate(context, R.layout.sayhi_pager_error, null);
        mErrorView.findViewById(R.id.error_btn_retry).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        this.addView(mErrorView);
        mEmptyView = View.inflate(context, R.layout.sayhi_paper_empty, null);
        this.addView(mEmptyView);
        mOffNetView = View.inflate(context, R.layout.sayhi_paper_offnet, null);
        mOffNetView.findViewById(R.id.offnet_error).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        this.addView(mOffNetView);
        refreshUI();
    }


    /**
     * 刷新视图
     */
    public void refreshUI() {
        mLoadingView.setVisibility((mCurState == STATE_LODING) || (mCurState == STATE_NONE) ? View.VISIBLE : View.GONE);
        mErrorView.setVisibility(mCurState == STATE_ERROR ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(mCurState == STATE_EMPTY ? View.VISIBLE : View.GONE);
        mOffNetView.setVisibility(mCurState == STATE_OFFNET ? View.VISIBLE : View.GONE);

        if (mSuccessView == null && mCurState == STATE_SUCCESS) {
            //创建成功视图
            mSuccessView = initSuccess();
            this.addView(mSuccessView);
        }
        if (null != mSuccessView) {
            mSuccessView.setVisibility(mCurState == STATE_SUCCESS ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 主动加载视图
     */
    public void loadData() {
        //默认进入状态等于 none
        if (mCurState != STATE_SUCCESS && mCurState != STATE_LODING) {
            int state = STATE_LODING;
            mCurState = state;
            //刷新视图
            refreshUI();
            //子线程加载数据， 主线程更新视图
            Observable.create(new Observable.OnSubscribe<LoadResult>() {
                @Override
                public void call(Subscriber<? super LoadResult> subscriber) {
                    LoadResult tempState = initData();
                    subscriber.onNext(tempState);
                    subscriber.onCompleted();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<LoadResult>() {
                        @Override
                        public void call(LoadResult loadResult) {
                            mCurState = loadResult.getState();
                            refreshUI();
                        }
                    });
        }
    }


    /**
     * 加载数据
     *
     * @return 返回状态根据状态加载响应视图
     */
    protected abstract LoadResult initData();


    /**
     * 创建成功视图
     *
     * @return
     */
    protected abstract View initSuccess();

    /**
     * 加载返回的几种结果
     */
    public enum LoadResult {

        SUCCESS(STATE_SUCCESS), ERROR(STATE_ERROR), EMPTY(STATE_EMPTY), OFFNET(STATE_OFFNET);
        int state;

        public int getState() {
            return state;
        }

        private LoadResult(int state) {
            this.state = state;
        }
    }


    /**
     * 在想重新加载该布局,并且不想直接修改时，调用该方法
     *
     * @return
     */
    public View getmSuccessView() {
        if (mSuccessView != null) {
            return mSuccessView;
        }
        return null;
    }

    /**
     * 在想重新加载该布局,并且不想直接修改时，调用该方法
     * <br>使用:  1、loadingPager.mCurState = STATE_NONE;<br>
     * 2、loadingPager.removeView(loadingPager.getmSuccessView());<br>
     * 3、loadingPager.setmSuccessView(null);<br>
     * 4、loadingPager.loadData();<br>
     *
     * @param mSuccessView
     */
    public void setmSuccessView(View mSuccessView) {
        this.mSuccessView = mSuccessView;
    }
}
