package com.example.module.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Zhenxi on 2020-07-22
 */
public class NetUtils {
    public static OkHttpClient getOkHttpClient() {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(CLogUtils::e).setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(200, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);//设置失败重连

        builder.addNetworkInterceptor(loggingInterceptor);


        return builder.build();
    }
}
