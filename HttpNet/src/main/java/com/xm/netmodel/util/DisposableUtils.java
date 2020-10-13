package com.xm.netmodel.util;

import com.xm.netmodel.helder.ResultThrowable;
import com.xm.netmodel.interfaces.HttpDataResultCallBack;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * <pre>
 *     author : Jerry
 *     time   : 2020/06/03
 *     desc   : RXJava操作工具类
 *     version: 1.0
 * </pre>
 */
public class DisposableUtils {

    //RXJava内存泄露处理
    protected CompositeDisposable mCompositeDisposable;

    /**
     * 添加返回请求数据DATA的Observable
     * */
    public void addDataObservable(Observable observable, HttpDataResultCallBack result) {
        if (null == result) {
            throw new NullPointerException("DATA RESULT CALL BACK IS NULL !");
        }
        if (null == mCompositeDisposable) {
            mCompositeDisposable = new CompositeDisposable();
        }
        if (null != observable) {
            Disposable subscribe = observable.compose(RxUtil.rxSchedulers())
                    .compose(RxUtil.exceptionTransformer())
                    .doOnSubscribe(disposable -> {
                        result.onSubscribe();
                    }).doOnComplete(() -> {
                        result.onComplete();
                    }).subscribe(result.getDataConsumer()
                            , (Consumer<ResultThrowable>) throwable -> {
                                result.onFailure(throwable.code, throwable.message, throwable.getMessage());
                                result.onComplete();
                            });
            mCompositeDisposable.add(subscribe);
        }
    }

    /**
     * 添加返回整个请求数据的Observable
     * */
//    public void addObservable(Observable observable, HttpResultCallBack result) {
//        if (null == result) {
//            throw new NullPointerException("DATA RESULT CALL BACK IS NULL !");
//        }
//        if (null == mCompositeDisposable) {
//            mCompositeDisposable = new CompositeDisposable();
//        }
//        if (null != observable) {
//            Disposable subscribe = observable.compose(RxUtil.rxSchedulers())
//                    .compose(RxUtil.exceptionTransformer())
//                    .doOnSubscribe(disposable -> {
//                        result.onSubscribe();
//                    }).doOnComplete(() -> {
//                        result.onComplete();
//                    }).subscribe(result.getConsumer()
//                            , (Consumer<ResultThrowable>) throwable -> {
//                                result.onFailure(throwable.code, throwable.message, throwable.getMessage());
//                                result.onComplete();
//                            });
//            mCompositeDisposable.add(subscribe);
//        }
//    }

    /**
     * 添加需要异步操作或者耗时操作的Observable
     * */
    public void addOtherObservable(RunImpl.RxMessageImpl rxMessage){
        if (null == mCompositeDisposable) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(RunImpl.getImpl().createRxRunning(rxMessage));
    }

    /**
     * 清除Disposable
     * */
    public void destroyDisposable(){
        if (null != mCompositeDisposable && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
            mCompositeDisposable.dispose();
        }
    }
}
