package com.xm.netmodel.impl;

import com.xm.netmodel.interfaces.INetView;
import com.xm.netmodel.util.RunImpl;

/**
 * Created by Android Studio.
 *
 * @author Jerry
 * Date: 2020/7/2 22:57
 * @description:
 */
public abstract class RunningImpl<T> extends RunImpl.RxMessageImpl<T> {

    private INetView mView;

    public RunningImpl() {

    }

    public RunningImpl(INetView mView) {
        this.mView = mView;
    }

    @Override
    public final void onSubscribe() {
        super.onSubscribe();
        if (null != mView) {
            mView.showLoadingDialog();
        }
    }

    @Override
    public final void onComplete() {
        super.onComplete();
        if (null != mView) {
            mView.hideLoadingDialog();
        }
    }

    @Override
    protected void onSuccess(T t) {

    }

    @Override
    protected void onFailure(int code, String msg) {

    }
}
