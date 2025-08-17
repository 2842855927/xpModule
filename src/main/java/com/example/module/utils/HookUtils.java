package com.example.module.utils;

import android.app.Activity;
import android.app.Application;

import com.example.module.LHook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * @author Zhenxi on 2020-07-28
 */
public class HookUtils {


    public static void HooktoString(String string){
        XposedHelpers.findAndHookMethod(String.class, "toString", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String result = (String) param.getResult();
                if(result.equals(string)){
                    AppUtils.getStackInfo();
                }
            }
        });
    }

    public static void HookDispatchActivityResumed(){
        XposedHelpers.findAndHookMethod(Application.class, "dispatchActivityResumed",
                Activity.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        LHook.topActivity = (Activity) param.args[0];
                        CLogUtils.e("切换 activity 成功");
                    }
                });
    }

    public static void HookcurrentTimeMillis(){
        //System.currentTimeMillis()
        XposedHelpers.findAndHookMethod(System.class, "currentTimeMillis",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        long result = (long) param.getResult();
                        CLogUtils.e("时间搓 currentTimeMillis"+result);
                    }
                });
    }

    public static void HooknanoTime(){
        //System.nanoTime()
        XposedHelpers.findAndHookMethod(System.class, "nanoTime",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        long result = (long) param.getResult();
                        CLogUtils.e("时间搓 nanoTime"+result);
                    }
                });
    }

}
