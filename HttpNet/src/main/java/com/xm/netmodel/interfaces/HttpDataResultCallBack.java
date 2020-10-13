package com.xm.netmodel.interfaces;

import android.text.TextUtils;

import com.xm.netmodel.Http;
import com.xm.netmodel.config.Config;
import com.xm.netmodel.config.HttpConfig;
import com.xm.netmodel.util.HttpDataUtils;
import com.xm.netmodel.util.StringUtils;

import io.reactivex.functions.Consumer;

/**
 * <pre>
 *     author : Jerry
 *     time   : 2020/06/03
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public abstract class HttpDataResultCallBack<T, M> {

    private String dataName;

    public HttpDataResultCallBack() {
        this.dataName = Config.getConfig().getDataName();
        if (TextUtils.isEmpty(dataName)) {
            throw new NullPointerException("DATA NAME IS EMPTY");
        }
    }

    protected abstract void onSuccess(M data, boolean isEmpty, String msg);

    public abstract void onFailure(int code, String msg, Object obj);

    public abstract void onError(int code, String msg);

    //请求执行方法
    public void onSubscribe() {

    }

    //请求完成执行方法
    public void onComplete() {

    }

    public final Consumer getDataConsumer() {
        return (Consumer<T>) response -> {
            try {
                // 通过反射获取model的真实类型
                int code = (int) HttpDataUtils.getValue(response, Config.getConfig().getCodeName());
                String str = StringUtils.getMsgData(response);
                if (Http.RESPONSE_OK == code) {
                    M data = (M) HttpDataUtils.getValue(response, dataName);
                    onSuccess(data, null == data, str);
                } else {
                    onFailure(code, str, response);
                }
            } catch (Exception e) {
                onError(Http.JSON_SYNTAX_ERR, e.getMessage());
            }
        };
    }

}
