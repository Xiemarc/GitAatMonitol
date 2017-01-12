package com.marc.marclibs.manager;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 描述：线程
 * 作者：Marc on 2016/7/9 09:43
 * 邮箱：aliali_ha@yeah.net
 */
public class ThreadPoolProxy {

    ThreadPoolExecutor mExecutor;//线程池 只需要创建一次。
    int mCorePoolSize;//核心线程数
    int mMaxmumPoolSize;//最大线程数
    long mKeepAliveTime;//保持时间

    /**
     * 构造方法 把参数依赖注入。外部传入
     *
     * @param mCorePoolSize
     * @param mMaxmumPoolSize
     * @param mKeepAliveTime
     */
    public ThreadPoolProxy(int mCorePoolSize, int mMaxmumPoolSize, long mKeepAliveTime) {
        this.mCorePoolSize = mCorePoolSize;
        this.mMaxmumPoolSize = mMaxmumPoolSize;
        this.mKeepAliveTime = mKeepAliveTime;
    }

    private ThreadPoolExecutor initThreadPoolExecutor() {
        if (mExecutor == null) {
            //双重检查加锁
            synchronized (ThreadPoolProxy.class) {
                if (mExecutor == null) {
                    TimeUnit unit = TimeUnit.MICROSECONDS;
                    BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();//无界队列
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();//线程工厂
                    ThreadPoolExecutor.AbortPolicy handler = new ThreadPoolExecutor.AbortPolicy(); //异常捕获器
                    mExecutor = new ThreadPoolExecutor(mCorePoolSize, mMaxmumPoolSize, mKeepAliveTime,
                            unit, workQueue, threadFactory, handler);
                }
            }
        }
        return mExecutor;
    }

    /**
     * 执行任务
     *
     * @param task
     */
    public void execute(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.execute(task);
    }

    /**
     * 提交任务
     *
     * @param task
     */
    public void submint(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.submit(task);
    }

    /**
     * 移除任务
     *
     * @param task
     */
    public void removeTask(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.remove(task);
    }

}
