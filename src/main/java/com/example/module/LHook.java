package com.example.module;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;


import com.example.module.utils.CLogUtils;
import com.example.module.utils.ClassUtils;
import com.example.module.utils.HiddenAPIEnforcementPolicyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import ctrip.business.BusinessRequestEntity;
//import ctrip.business.comm.RequestDataBean;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class LHook implements IXposedHookLoadPackage {


    public static boolean isNeedIntoMyDex = true;
    public static Context mContext;
    public static ClassLoader mLoader;
    public static Activity topActivity;
    private static Object GSON = null;
    private static View All = null;
    private static View verifyImageView = null;
    //方法所属class路径
    private static String ClassPath = "";
    private static String MethodName = "";
    //匹配对方so的名字
    private final String IntoSoName = "libTest.so";
    private String packageName = "com.kejian.one";
    //是否需要HookNaitive方法的 开关
    private boolean isNeedHookNative = true;
    private Class bin;
    //存放 这个 app全部的 classloader
    private ArrayList<ClassLoader> AppAllCLassLoaderList = new ArrayList<>();
    private Activity mActivity;

    XC_LoadPackage.LoadPackageParam mlpparam;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(packageName)) {
            mlpparam=lpparam;
            CLogUtils.e("发现被Hook的 App");



            HookLoadClass();
            HookAttach(lpparam);
        }
    }

    /**
     * 将自己的Dex 注入到对方进程
     * 利用父委托机制 替换掉源码里面的class
     */
//    private void intoMyCode() {
//        boolean addElements = FixElementsUtils.AddElements(mContext, this.getClass().getClassLoader());
//        CLogUtils.e("AddElements执行结果 "+addElements);

//    }
    private void HookLoadClass() {
        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass",
                String.class,
                //boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (param == null) {
                            return;
                        }
                        Class cls = (Class) param.getResult();
                        //在这块 做个判断 是否 存在 如果 不存在 在保存  否则 影响效率
                        //如果 不存在
                        if (cls == null) {
                            return;
                        }
                        isNeedAddClassloader(cls.getClassLoader());
                    }
                });

        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass",
                String.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        if (param == null) {
                            return;
                        }
                        Class cls = (Class) param.getResult();
                        //在这块 做个判断 是否 存在 如果 不存在 在保存  否则 影响效率
                        //如果 不存在
                        if (cls == null) {
                            return;
                        }
                        isNeedAddClassloader(cls.getClassLoader());
                    }
                });
    }

    /**
     * 对 每个 classloader进行判断
     *
     * @param classLoader 需要 的classloader
     * @return
     */
    private void isNeedAddClassloader(ClassLoader classLoader) {

        //先过滤掉系统类 ,如果是系统预装的 类也 不要
        if (classLoader.getClass().getName().equals("java.lang.BootClassLoader")) {
            return;
        }
        for (ClassLoader loader : AppAllCLassLoaderList) {
            if (loader.hashCode() == classLoader.hashCode()) {
                return;
            }
        }
        //CLogUtils.e("加入的classloader名字  " + classLoader.getClass().getName());
        AppAllCLassLoaderList.add(classLoader);
    }

    private void HookAttach(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(Application.class, "attach",
                Context.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                CLogUtils.e("走了 attachBaseContext方法 ");
                mContext = (Context) param.args[0];
                mLoader = mContext.getClassLoader();
                //注入自己的代码
//                intoMyCode();

                CLogUtils.e("拿到classloader");
                if (isNeedHookNative) {
                    intoMySo(null);
                }
//                if (isNeedHookNative) {
//                    //开始 注入 自己的 so 进行  Naitive 拦截
//                    initMySo();
//                    return;
//                }

                //HookMianInit();
            }
        });

//        XposedHelpers.findAndHookMethod(Application.class,
//                "onCreate",
//                new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//
//            }
//        });

        XposedHelpers.findAndHookMethod(Activity.class,
                "onCreate",
                Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

            }

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                mActivity = (Activity) param.thisObject;

            }
        });

    }


    private void HookMianInit() {
        if (ClassPath != null && !ClassPath.equals("")) {
            try {
                bin = Class.forName(ClassPath, true, mLoader);
                //bin2 = Class.forName(ClassPath2, true, mLoader);
//                            bin3 = Class.forName(ClassPath3, true, mLoader);
                if (bin == null) {
                    bin = Class.forName(ClassPath);
                    //bin2 = Class.forName(ClassPath2);
//                                bin3 = Class.forName(ClassPath3);
                }
                if (bin != null) {
                    CLogUtils.e("成功拿到 bin  ");
                    HookMain();
                } else {
                    CLogUtils.e("没有拿到bin 对象");
                }
            } catch (Throwable e) {
                e.printStackTrace();
                CLogUtils.e("没找到 Bin  开始执行 forEatchClassLoader   " + e.getMessage());
                for (ClassLoader mLoader2 : AppAllCLassLoaderList) {
                    try {
                        bin = Class.forName(ClassPath, true, mLoader2);
                        if (bin != null) {
                            HookMain();
                            return;
                        }
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                CLogUtils.e("forEatchClassLoader 循环完毕也没找到");
            }
        }
    }

    @SuppressWarnings("ALL")
    private void HookSoLoad() {
//        System.load();
//        System.loadLibrary();

        //ClassUtils.getClassMethodInfo(Runtime.class);
        int version = android.os.Build.VERSION.SDK_INT;
        CLogUtils.e("当前系统 版本号 " + version);
        //android 9.0没有 doLoad 方法
        if (version >= 28) {
//            HiddenAPIEnforcementPolicyUtils.passApiCheck();
            try {
                XposedHelpers.findAndHookMethod(Runtime.class,
                        "nativeLoad",
                        String.class,
                        ClassLoader.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                String name = (String) param.args[0];
                                //Hook 要加载的 so
                                //当 加载 的 so 是 要 加载的 就加载自己的  libcocos2dlua
                                if (param.hasThrowable() || name == null || !name.endsWith(IntoSoName)) {
                                    return;
                                }
                                try {

                                    //如果 包含 加载的 so 就 加载自己的 在 自己so里面 再打开 目标 so
                                    if (((String) param.args[0]).contains(IntoSoName)) {
                                        //注入 自己的 so 吧 classloader进行 传入
                                        intoMySo(param.args[1]);
                                    }
                                } catch (Throwable e) {
                                    CLogUtils.e("initMySo 异常" + e.getMessage());
                                }
                                CLogUtils.e("java层执行完毕");
                            }
                        });
            } catch (Throwable e) {
                CLogUtils.e("Hook 28以上出现异常 " + e.toString());
                e.printStackTrace();
            }
            CLogUtils.e("Hook 9.0以上 load成功");
        } else {
            //小于9.0
            try {
                XposedHelpers.findAndHookMethod(Runtime.class,
                        "doLoad",
                        String.class,
                        ClassLoader.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                String name = (String) param.args[0];

                                //Hook 要加载的 so
                                //当 加载 的 so 是 要 加载的 就加载自己的  libcocos2dlua
                                if (param.hasThrowable() || name == null || !name.endsWith(IntoSoName)) {
                                    return;
                                }
                                try {
                                    if (android.os.Build.VERSION.SDK_INT <= 19) {
                                        CLogUtils.e("走了 4.4的  load方法 ");
                                        System.load(getMySoPath());
                                    } else {
                                        //如果 包含 加载的 so 就 加载自己的 在 自己so里面 再打开 目标 so
                                        if (((String) param.args[0]).contains(IntoSoName)) {
                                            //注入 自己的 so 吧 classloader进行 传入
                                            intoMySo(param.args[1]);
                                        }
                                    }
                                } catch (Exception e) {
                                    CLogUtils.e("异常" + e.getMessage());
                                }
                                CLogUtils.e("java层执行完毕");
                            }
                        });
            } catch (Exception e) {
                CLogUtils.e("Hook doLoad出现异常 " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void HookMain() {


    }

    /**
     * 遍历当前进程的Classloader 尝试进行获取指定类
     */
    private Class getClass(String className) {
        Class<?> aClass = null;
        try {
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException classNotFoundE) {

                try {
                    aClass = Class.forName(className, false, mLoader);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (aClass != null) {
                    return aClass;
                }
                try {
                    for (ClassLoader classLoader : AppAllCLassLoaderList) {
                        try {
                            aClass = Class.forName(className, false, classLoader);
                        } catch (Throwable e) {
                            continue;
                        }
                        if (aClass != null) {
                            return aClass;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            return aClass;
        } catch (Throwable e) {

        }
        return null;
    }


    /**
     * 获取模块的So文件路径
     */
    private String getMySoPath() {


        CLogUtils.e("开始注入自己的 So getMySoPath   ");


        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> pkgList = pm.getInstalledPackages(0);
        if (pkgList.size() > 0) {
            for (PackageInfo pi : pkgList) {
                if (pi.applicationInfo.publicSourceDir.startsWith("/data/app/" + BuildConfig.APPLICATION_ID)) {
                    //data/app/com.lyh.nkddemo-YuNFiNvInJyE3ahHYBXAQw==/base.apk
                    String path = pi.applicationInfo.publicSourceDir.
                            replace("base.apk", "lib/arm/libLVmp.so");
                    CLogUtils.e("getMySoPath 对应的路径是" + path);
                    return path;
                }
            }
        }

        CLogUtils.e("没找到 MySo注入的 路径 ");
        return null;
    }


    /**
     * 在这里 把自己的 so进行 注入
     *
     * @param arg
     */
    private void intoMySo(Object arg) {
        String path = getMySoPath();
        int version = android.os.Build.VERSION.SDK_INT;
        CLogUtils.e("当前系统 版本号 " + version);
        //android 9.0没有 doLoad 方法
        if (version >= 28) {
            XposedHelpers.callMethod(Runtime.getRuntime(), "nativeLoad", path, arg);
        }else {
            XposedHelpers.callMethod(Runtime.getRuntime(), "doLoad", path, arg);
        }
        CLogUtils.e("intoMySo 注入成功");
    }

}

