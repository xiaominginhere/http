package com.xm.netmodel.config;

import android.text.TextUtils;

/**
 * Created by BaseMvpLibs.
 * Author: Jerry.
 * Date: 2020/10/12:15:41.
 * Desc:
 */
public class Config {

    private String codeName;
    private String dataName;
    private String msgName = "msg";

    private static Config config;

    public synchronized static Config getConfig() {
        if (null == config) {
            synchronized (Config.class) {
                if (null == config) {
                    config = new Config();
                }
            }
        }
        return config;
    }

    /**
     * 初始化工具参数
     */
    public Config initConfigData(String codeName, String dataName) {
        if (TextUtils.isEmpty(codeName)) {
            throw new NullPointerException("code name is empty,init failure");
        }
        if (TextUtils.isEmpty(dataName)) {
            throw new NullPointerException("code name is empty,init failure");
        }
        this.codeName = codeName;
        this.dataName = dataName;
        return this;
    }

    /**
     * 初始化工具参数
     */
    public Config setMsg(String msgName) {
        if (!TextUtils.isEmpty(msgName)) {
            this.msgName = msgName;
        }
        return this;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getDataName() {
        return dataName;
    }

    public String getMsgName() {
        return msgName;
    }
}
