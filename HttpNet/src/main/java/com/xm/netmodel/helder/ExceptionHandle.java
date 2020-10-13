package com.xm.netmodel.helder;

import android.net.ParseException;

import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.net.ConnectException;

import retrofit2.HttpException;

/**
 * Created by goldze on 2017/5/11.
 */
public class ExceptionHandle {

    private static final int LOGIN_AGAIN = 100;
    private static final int FORBIDDEN = 400;
    private static final int NEXT_FOUND = 201;
    private static final int NOT_FOUND = 404;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int SERVICE_UNAVAILABLE = 503;

    public static ResultThrowable handleException(Throwable e) {
        ResultThrowable ex;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ResultThrowable(e, ERROR.HTTP_ERROR);
            switch (httpException.code()) {
                case LOGIN_AGAIN:
                    ex.message = "重新登录";
                    break;
                case FORBIDDEN:
                    ex.message = "请求失败";
                    break;
                case NEXT_FOUND:
                    ex.message = "下一操作提示";
                    break;
                case NOT_FOUND:
                    ex.message = "接口不存在";
                    break;
                case INTERNAL_SERVER_ERROR:
                    ex.message = "网络不可用";
//                    ex.message = "服务器内部错误";
                    break;
                case SERVICE_UNAVAILABLE:
                    ex.message = "服务器不可用";
                    break;
                default:
                    ex.message = "网络错误";
                    break;
            }
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException || e instanceof MalformedJsonException) {
            ex = new ResultThrowable(e, ERROR.PARSE_ERROR);
            ex.message = "解析错误";
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new ResultThrowable(e, ERROR.NETWORD_ERROR);
            ex.message = "连接失败";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLException) {
            ex = new ResultThrowable(e, ERROR.SSL_ERROR);
            ex.message = "证书验证失败";
            return ex;
        } else if (e instanceof ConnectTimeoutException) {
            ex = new ResultThrowable(e, ERROR.TIMEOUT_ERROR);
            ex.message = "连接超时";
            return ex;
        } else if (e instanceof java.net.SocketTimeoutException) {
            ex = new ResultThrowable(e, ERROR.TIMEOUT_ERROR);
            ex.message = "连接超时";
            return ex;
        } else if (e instanceof java.net.UnknownHostException) {
            try {
                ex = new ResultThrowable(e, ERROR.TIMEOUT_ERROR);
            }catch (Exception es){
                ex = new ResultThrowable(es, ERROR.TIMEOUT_ERROR);
            }
            ex.message = "主机地址未知";
            return ex;
        } else {
            ex = new ResultThrowable(e, ERROR.UNKNOWN);
            ex.message = "未知错误";
            return ex;
        }
    }


    /**
     * 约定异常 这个具体规则需要与服务端或者领导商讨定义
     */
    class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1001;
        /**
         * 网络错误
         */
        public static final int NETWORD_ERROR = 1002;
        /**
         * 协议出错
         */
        public static final int HTTP_ERROR = 1003;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1005;

        /**
         * 连接超时
         */
        public static final int TIMEOUT_ERROR = 1006;
    }

}

