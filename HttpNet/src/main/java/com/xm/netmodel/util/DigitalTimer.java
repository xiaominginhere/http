package com.xm.netmodel.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * <pre>
 *     author : Jerry
 *     time   : 2020/06/04
 *     desc   : 定时器
 *     version: 1.0
 * </pre>
 */
public abstract class DigitalTimer {

    //RXJava内存泄露处理
    protected CompositeDisposable mCompositeDisposable;
    private Disposable subscribe;
    private Observable observable;

    /**
     * @param millisInFuture 时间间隔
     * @param isSeconds      是否是秒级的执行时间单位
     */
    public DigitalTimer(long millisInFuture, long totalMillisInFuture) {
        mCompositeDisposable = new CompositeDisposable();
        initTimer(millisInFuture,totalMillisInFuture);
    }

    @SuppressLint("CheckResult")
    private void initTimer(long millisInFuture,long totalMillisInFuture) {
        observable = Observable.interval(millisInFuture, TimeUnit.MILLISECONDS)
                .compose(RxUtil.exceptionTransformer()).compose(RxUtil.rxSchedulers())
                .doOnNext((Consumer<Long>) time -> {
                    time = totalMillisInFuture - time;
                    if (0 == time) {
                        stopAndDestroyTimer();
                        return;
                    }
                    onTick(time);
                }).doOnError(throwable -> {
                    stopAndDestroyTimer();
                    Log.e("timer", "throwable:" + throwable.toString());
                });
    }

    public final DigitalTimer start() {
        subscribe = observable.subscribe();
        mCompositeDisposable.add(subscribe);
        return this;
    }

    protected abstract void onTick(long time);

    protected void onComplete() {
    }

    /**
     * 清除Disposable
     */
    public final void stopAndDestroyTimer() {
        onComplete();
        if (null != observable) {
            observable = null;
        }
        if (null != subscribe && !subscribe.isDisposed()) {
            subscribe.dispose();
            subscribe = null;
        }
        if (null != mCompositeDisposable && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
            mCompositeDisposable.dispose();
        }
    }
}
