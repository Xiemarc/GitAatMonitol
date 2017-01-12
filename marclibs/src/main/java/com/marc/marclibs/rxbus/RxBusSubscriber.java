package com.marc.marclibs.rxbus;

import com.marc.marclibs.utils.logger.Logger;

import rx.Subscriber;

public abstract class RxBusSubscriber<T> extends Subscriber<T> {

    @Override
    public void onNext(T t) {
        try {
            onEvent(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        Logger.e(e.getMessage());
    }

    protected abstract void onEvent(T t) throws Exception;
}