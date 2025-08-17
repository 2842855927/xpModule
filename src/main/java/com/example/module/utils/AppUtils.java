package com.example.module.utils;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Zhenxi on 2020-07-16
 */
public class AppUtils {


    public static void getStackInfo() {
        try {
            throw new Exception("");
        } catch (Exception e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                CLogUtils.e(" 栈信息  " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "  行数   " + stackTraceElement.getLineNumber());
            }
        }
    }


    public static void getMapInfo(Map<String,String> map) {
        if(map==null){
            CLogUtils.e("getMapInfo->map null");
            return;
        }
        for(Map.Entry<String, String> stringEntry: map.entrySet()){
            CLogUtils.e("Map Key-> "+stringEntry.getKey()+"  Value-> "+stringEntry.getValue());
        }
        CLogUtils.e("打印Map 结束");
    }
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
