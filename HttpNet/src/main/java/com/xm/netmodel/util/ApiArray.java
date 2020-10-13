package com.xm.netmodel.util;

import com.xm.netmodel.model.RequestApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaseMvpLibs.
 * Author: Jerry.
 * Date: 2020/10/12:16:31.
 * Desc:
 */
public class ApiArray {

    private List<RequestApi> apis = new ArrayList<>();

    /**
     * 添加请求api
     * @param api
     */
    public void addApi(RequestApi api){
        if (null == api) {
            return;
        }
        apis.add(api);
    }

    /**
     * 移除请求api
     * @param api
     */
    public void removeApi(RequestApi api){
        if (null == apis || apis.isEmpty() || null == api) {
            return;
        }
        api.cancelRequest();
        if (apis.contains(api)) {
            apis.remove(api);
        }
    }

    /**
     * 清除所有请求api
     */
    public void clean(){
        if (null == apis || apis.isEmpty()){
            return;
        }
        for (RequestApi api : apis) {
            api.cancelRequest();
        }
        apis.clear();
    }
}
