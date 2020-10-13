package com.xm.netmodel;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;
import com.xm.netmodel.config.Config;
import com.xm.netmodel.config.HttpConfig;
import com.xm.netmodel.helder.BaseResponse;
import com.xm.netmodel.helder.ResponseBodyInterceptor;
import com.xm.netmodel.util.HttpDataUtils;
import com.xm.netmodel.util.L;
import com.xm.netmodel.util.StringUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Http {

    /**
     * 需要设置参数
     */
    private static String BASE_URL = "";
    public static int RESPONSE_OK = 0;  //请求成功码
    public static final int JSON_SYNTAX_ERR = -10001; //json解析异常错误码
    private static int RESPONSE_LOGIN_ERR = -1; //登录过期码

    private final String TAGS = "HttpApi";
    private static Http http;
    private static OnLoginOutListener onLoginOutListener;
    private static boolean isVerifyToken = false;

    private final int CONNECT_TIME_OUT = 60; //连接超时时间
    private final int WRITE_OR_READ_TIME_OUT = 15; //读写超时时间

    private static final String TOKEN_KEY = "http_api_token_key";//保存token秘钥

    private String mToken = "";

    private Gson gson = null;

    /**
     * @param cls     api class
     * @param dataCls json 解析class
     */
    public static <T> T getAPPInstance(Class<T> cls, Class... dataCls) {
        return getHttp().createBaseApi(cls, dataCls);
    }

    private static synchronized Http getHttp() {
        if (http == null) {
            http = new Http();
        }
        return http;
    }

    /**
     * 初始化
     *
     * @param baseUrl
     * @param responseOk
     * @param responseLoginErr
     * @param isVerify         是否使用Authorization 验证token
     */
    public static void initHttp(String baseUrl, int responseOk, int responseLoginErr, boolean isVerify) {
        BASE_URL = baseUrl;
        RESPONSE_OK = responseOk;
        RESPONSE_LOGIN_ERR = responseLoginErr;
        isVerifyToken = isVerify;
    }

    private HttpLoggingInterceptor.Logger loggerInterceptor = message -> {
        if (message.length() > 2500) {
            L.i(TAGS, message);
        } else {
            L.e(TAGS, message);
        }
        if (null != onLoginOutListener && StringUtils.isJsonFormat(message)) {
            BaseResponse<Object> o = (BaseResponse<Object>) StringUtils.parserJson(message, BaseResponse.class);
            if (o.getStatus() == RESPONSE_LOGIN_ERR) {
                onLoginOutListener.onLoginOut(o.getMsg());
            }
        }
    };

    private Interceptor requestHeaderInterceptor = chain -> {
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("Content-Type", "application/json;charset=UTF-8");
//        builder.addHeader("X-Requested-With", "XMLHttpRequest");

        //      请求头添加数据
        if (!TextUtils.isEmpty(getToken())) {
            if (isVerifyToken) {
                builder.addHeader("Authorization", "Bearer " + mToken); //http2 认证
            } else {
                builder.addHeader("token", mToken);
            }
            L.e(TAGS, "token:" + mToken);
        }

        return chain.proceed(builder.build());
    };

    //    创建主服务器地址的Retrofit
    public <T> T createBaseApi(Class<T> cls, Class... dataCls) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //        日志拦截
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(loggerInterceptor);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        //        请求头拦截
        builder.addInterceptor(requestHeaderInterceptor);

        if (null != dataCls && dataCls.length > 0) {
            if (null == gson) {
                gson = new Gson();
            }
            //添加消息拦截头
            builder.addInterceptor(new ResponseBodyInterceptor() {
                /**
                 * 过滤json解析错误的异常
                 * */
                @Override
                protected Response intercept(Response response, String url, String body) {
                    return response.newBuilder().body(checkBody(dataCls[0], response.body().contentType(), body)).build();
                }
            });
        }

        //        设置超时
        builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(WRITE_OR_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_OR_READ_TIME_OUT, TimeUnit.SECONDS);

        //        错误重连
        builder.retryOnConnectionFailure(true);

        //    线上和测试服的切换在这里
        Retrofit mRetrofit = new Retrofit.Builder().client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)// 测试服
                .build();

        return mRetrofit.create(cls);
    }

    /**
     * 检测http返回的json数据是否有问题
     *
     * @param dataCls
     * @param mediaType
     * @param body
     * @return
     */
    private ResponseBody checkBody(Class dataCls, MediaType mediaType, String body) {
        if (TextUtils.isEmpty(body) || !StringUtils.isJsonFormat(body)) {
            //设置空数据的json字符串返回
            return ResponseBody.create(mediaType
                    , gson.toJson(createResultObject(dataCls, JSON_SYNTAX_ERR, "JSON OBJECT IS EMPTY!")));
        } else {
            try {
                gson.fromJson(body, dataCls);
            } catch (Exception e) {
                L.e(TAGS, "checkBody error:" + e.getMessage());
                L.e(TAGS, body);
                try {
                    JSONObject object = new JSONObject(body);
                    int code = object.getInt(Config.getConfig().getCodeName());
                    String msgName = Config.getConfig().getMsgName();
                    String str = "";
                    if (object.has(msgName)) {
                        str = object.getString(msgName);
                        L.e(TAGS, "str:" + str);
                    }
                    return ResponseBody.create(mediaType
                            , gson.toJson(createResultObject(dataCls, 200 == code ? JSON_SYNTAX_ERR : code, str)));
                } catch (Exception e1) {
                    L.e("checkBody error more:" + e1.getMessage());
                }
            }
        }
        return ResponseBody.create(mediaType, body);
    }

    private Object createResultObject(Class dataCls, int jsonSyntaxErr, String msg) {
        try {
            Object instance = dataCls.newInstance();
            HttpDataUtils.setValue(instance, jsonSyntaxErr,Config.getConfig().getCodeName());
            String msgName = Config.getConfig().getMsgName();
            HttpDataUtils.setValue(instance, msg, msgName);
            return instance;
        } catch (Exception e) {
            L.e("createResultObject error:" + e.getMessage());
        }
        return null;
    }

    /**
     * 创建MultipartBody
     *
     * @param name 表单名称
     * @param file 文件信息
     */
    public static MultipartBody.Part createMultipartBodyPart(String name, File file) {
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData(name, file.getName(), requestFile);
    }

    public static void setOnLoginOutListener(OnLoginOutListener onLoginOutListener) {
        Http.onLoginOutListener = onLoginOutListener;
    }

    /**
     * 获取http请求token
     */
    public String getToken() {
        if (TextUtils.isEmpty(mToken) && null != Hawk.get(TOKEN_KEY)) {
            mToken = Hawk.get(TOKEN_KEY);
        }
        L.e("mToken:" + mToken);
        return mToken;
    }

    /**
     * 设置http请求token，保存到缓存
     */
    public static void setToken(String mToken) {
        if (TextUtils.isEmpty(mToken)) {
            Hawk.delete(TOKEN_KEY);
        }
        Hawk.put(TOKEN_KEY, mToken);
        getHttp().mToken = mToken;
    }

    /**
     * 判断是否已经登录
     *
     * @return
     */
    public static boolean isTokenEmpty() {
        return TextUtils.isEmpty(getHttp().getToken());
    }

    public interface OnLoginOutListener {
        void onLoginOut(String content);
    }

}
