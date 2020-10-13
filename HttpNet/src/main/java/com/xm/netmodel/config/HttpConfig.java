package com.xm.netmodel.config;

import android.content.Context;
import android.text.TextUtils;

import com.orhanobut.hawk.Hawk;
import com.xm.netmodel.Http;

/**
 * @ClassName BuildLibConfig
 * @Description TODO
 * @Author Jerry
 * @Date 2020/5/4 20:41
 * @Version 1.0
 */
public class HttpConfig {

    private static HttpConfig config;
    private int responseOk = 0;
    private boolean isVerify = false;

    private HttpConfig() {
    }

    public static HttpConfig init(Context mContext, String jsonCodeKey, String jsonDataKey) {
        Hawk.init(mContext).build();
        Config.getConfig().initConfigData(jsonCodeKey, jsonDataKey);
        if (null == config) {
            config = new HttpConfig();
        }
        return config;
    }

    public HttpConfig setJsonMsgKeyName(String msg) {
        Config.getConfig().setMsg(msg);
        return config;
    }

    public HttpConfig setResponseOk(int responseOk) {
        this.responseOk = responseOk;
        return this;
    }

    public HttpConfig setVerify(boolean verify) {
        isVerify = verify;
        return this;
    }

    public void builder(String baseUrl, int loginOutErr) {
        Http.initHttp(baseUrl, responseOk, loginOutErr, isVerify);
    }
}
