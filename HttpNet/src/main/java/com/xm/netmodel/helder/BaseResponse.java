package com.xm.netmodel.helder;

/**
 * @ClassName BasicRespense
 * @Description TODO
 * @Author Jerry
 * @Date 2020/5/4 0:39
 * @Version 1.0
 */
public class BaseResponse<T> {

    private int status;
    private String msg;
    private T data;

    public final T getData() {
        return data;
    }

    public final int getStatus() {
        return status;
    }

    public final String getMsg() {
        return msg;
    }

    public final void setStatus(int status) {
        this.status = status;
//        setCode(status);
    }

    public final void setMsg(String msg) {
        this.msg = msg;
//        setMessage(msg);
    }

    public final void setData(T data) {
        this.data = data;
//        setContentData(data);
    }

//    protected abstract void setCode(int status);
//
//    protected abstract void setMessage(String msg);
//
//    protected abstract void setContentData(T data);
}
