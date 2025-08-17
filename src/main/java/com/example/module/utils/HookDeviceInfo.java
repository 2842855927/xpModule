package com.example.module.utils;

import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Zhenxi on 2020-09-08
 */
public class HookDeviceInfo {

    private static String[] devives = new String[]{
            "MI 9", "MI 8", "MI 6", "MI 5", "MI 4", "MI 3","MI 10",
            "OPPO A5", "OPPO A37", "OPPO A59", "OPPO A59s", "OPPO A57", "OPPO A77", "OPPO R15",
            "OPPO A79", "OPPO A73", "OPPO R9", "OPPO R9plus",
            "OPPO R11", "OPPO R11plus", "OPPO R11s", "OPPO R11splus", "OPPO R11", "OPPO R15"
    };


    private static String RandomIMEI=getIMEI();


    public  static   void   HookIMEI(ClassLoader mloader){
        try {
            XposedBridge.hookAllMethods(TelephonyManager.class, "getImei", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(RandomIMEI);
                    CLogUtils.e("获取getImei被调用了  返回的 结果 是 " + param.getResult());

                    //AppUtils.getStackInfo();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            CLogUtils.e("获取getImei被调用了  error" + e.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(
                    XposedHelpers.findClass("com.android.internal.telephony.gsm.GSMPhone",mloader),
                    "getDeviceId", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(RandomIMEI);
                    CLogUtils.e( "小于22版本号的 GSMPhone 设备ID 获取" + param.getResult());
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            XposedHelpers.findAndHookMethod(
                    XposedHelpers.findClass("com.android.internal.telephony.PhoneSubInfo",mloader),
                    "getDeviceId",
                    new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(RandomIMEI);

                    CLogUtils.e( "PhoneSubInfo 设备ID 获取" + param.getResult());
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            XposedHelpers.findAndHookMethod(
                    XposedHelpers.findClass("com.android.internal.telephony.PhoneProxy",mloader),
                     "getDeviceId", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    param.setResult(RandomIMEI);

                    CLogUtils.e( "PhoneProxy 设备ID 获取" + param.getResult());
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机IMEI
     *
     * @return
     */
    private static String getIMEI() {// calculator IMEI
        int r1 = 1000000 + new Random().nextInt(9000000);
        int r2 = 1000000 + new Random().nextInt(9000000);
        String input = r1 + "" + r2;
        char[] ch = input.toCharArray();
        int a = 0, b = 0;
        for (int i = 0; i < ch.length; i++) {
            int tt = Integer.parseInt(ch[i] + "");
            if (i % 2 == 0) {
                a = a + tt;
            } else {
                int temp = tt * 2;
                b = b + temp / 10 + temp % 10;
            }
        }
        int last = (a + b) % 10;
        if (last == 0) {
            last = 0;
        } else {
            last = 10 - last;
        }
        return input + last;
    }
    private void HookDeviceName(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        String devive = devives[new Random().nextInt(devives.length - 1)];

        try {
            try {
                XposedHelpers.setStaticObjectField(Build.class, "MODEL", devive);
                CLogUtils.e("Hook 机型成功");
            } catch (Exception e) {
                e.printStackTrace();
            }

            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("android.os.Build",loadPackageParam.classLoader),
                    "getString",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            String arg = (String) param.args[0];
                            switch (arg) {
                                case "ro.product.model":
                                    //MI 9
                                    param.setResult(devive);
                                    CLogUtils.e( "获取手机model返回的结果是" + param.getResult());
                                    break;
                            }
                        }
                    });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
