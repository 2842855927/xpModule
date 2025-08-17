package com.example.module.utils;

import com.google.gson.Gson;

/**
 * Created by Lyh on
 * 2019/11/12
 */
public class GsonUtils {
    private static Gson gson = new Gson();

    public static  Object getClasss(String jsonString, Class c) {
        try {
            return  gson.fromJson(jsonString, c);
        } catch (Throwable e) {
            CLogUtils.e("Gson 构建 Object  error "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public static String obj2str(Object object) {
        try {
           return gson.toJson(object);
        } catch (Throwable e) {
            CLogUtils.e("Bean转Json失败 "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
