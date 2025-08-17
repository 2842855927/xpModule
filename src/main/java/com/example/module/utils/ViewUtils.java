package com.example.module.utils;

import android.view.View;

/**
 * @author Zhenxi on 2020-07-17
 */
public class ViewUtils {
    /**
     * 获取当前View 相对屏幕中心点坐标
     *
     * @return
     * [0]->X
     * [1]->Y
     */
    public static float[] getLocationOnScreen(View view) {
        int[] locs = new int[2];
        view.getLocationOnScreen(locs);
        //中心点
        float x = locs[0] + view.getWidth() / 2;
        float y = locs[1] + view.getHeight() / 2;

        float[] ret = new float[2];
        ret[0] = x;
        ret[1] = y;
        return ret;
    }

    /**
     * 获取当前View 相对屏幕中心点坐标
     *
     * @return
     * [0]->X
     * [1]->Y
     */
    public static float[] getLocationInWindow(View view) {
        int[] locs = new int[2];
        view.getLocationInWindow(locs);
        //中心点
        float x = locs[0] + view.getWidth() / 2;
        float y = locs[1] + view.getHeight() / 2;

        float[] ret = new float[2];
        ret[0] = x;
        ret[1] = y;
        return ret;
    }

}
