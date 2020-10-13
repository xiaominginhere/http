package com.xm.netmodel.model;

import com.xm.netmodel.helder.ResultThrowable;
import com.xm.netmodel.interfaces.HttpDataResultCallBack;
import com.xm.netmodel.util.RxUtil;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by BaseMvpLibs.
 * Author: Jerry.
 * Date: 2020/10/12:16:16.
 * Desc:
 */
public abstract class RequestApi {

    private CompositeDisposable mCompositeDisposable = null;

    public RequestApi() {

    }

    protected abstract Observable request();

    protected abstract HttpDataResultCallBack initResultCallBack();

    private void runRequest() {
        Observable request = request();
        if (null == request) {
            throw new NullPointerException("OBSERVABLE NULL OR CALLBACK NULL");
        }
    }

    public final void run() {
        runRequest();
    }

    private void start() {
        Observable request = request();
        HttpDataResultCallBack resultCallBack = initResultCallBack();
        if (null == request || null == resultCallBack) {
            throw new NullPointerException("DATA RESULT CALL BACK IS NULL !");
        }
        mCompositeDisposable = new CompositeDisposable();
        Disposable subscribe = request.compose(RxUtil.rxSchedulers())
                .compose(RxUtil.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    resultCallBack.onSubscribe();
                }).doOnComplete(() -> {
                    resultCallBack.onComplete();
                    cancelRequest();
                }).subscribe(resultCallBack.getDataConsumer()
                        , (Consumer<ResultThrowable>) throwable -> {
                            resultCallBack.onFailure(throwable.code, throwable.message, throwable.getMessage());
                            resultCallBack.onComplete();
                            cancelRequest();
                        });
        mCompositeDisposable.add(subscribe);
    }

    public final void cancelRequest() {
        if (null != mCompositeDisposable) {
            mCompositeDisposable.clear();
            mCompositeDisposable.dispose();
            mCompositeDisposable = null;
        }
    }
}
