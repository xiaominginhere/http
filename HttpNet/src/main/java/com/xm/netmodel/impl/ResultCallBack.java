package com.xm.netmodel.impl;

import com.xm.netmodel.interfaces.HttpDataResultCallBack;
import com.xm.netmodel.interfaces.INetView;
import com.xm.netmodel.util.L;

/**
 * Created by Android Studio.
 *
 * @author Jerry
 * Date: 2020/7/2 22:51
 * @description:
 */
public abstract class ResultCallBack<T, M> extends HttpDataResultCallBack<T, M> {

    private INetView mView;

    public ResultCallBack() {
    }

    public ResultCallBack(INetView mView) {
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
    public void onFailure(int code, String msg, Object obj) {
        L.e("error", "result:" + code + ">>>>" + msg + ">>>" + obj);
    }

    @Override
    public void onError(int code, String msg) {
        L.e("error", "error:" + code + ">>>>" + msg);
    }


}
