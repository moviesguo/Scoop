package com.mfw.base.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;


public class DPIUtil {

    public static final int _20dp = DPIUtil.dip2px(20);
    public static final int _10dp = DPIUtil.dip2px(10);
    public static final int _5dp = DPIUtil.dip2px(5);
    public static final int _2dp = DPIUtil.dip2px(2);
    public static final int _3dp = DPIUtil.dip2px(3);
    public static final int _15dp = DPIUtil.dip2px(15);
    public static final int _dp8 = DPIUtil.dip2px(8);
    public static final int _dp4 = DPIUtil.dip2px(4);
    public static final int _dp12 = DPIUtil.dip2px(12);
    public static final int _100dp = DPIUtil.dip2px(100);
    public static final int _1dp = DPIUtil.dip2px(1);
    public static final int _7p5 = DPIUtil.dip2px(7.5f);

    /**
     * 屏幕宽度
     */
    private static int DisplayWidthPixels = 0;
    /**
     * 屏幕高度
     */
    private static int DisplayHeightPixels = 0;

    public static int dip2px(float dipValue) {
        return (int) (dipValue * 2 + 0.5f);
    }

    public static int dip2FloorPx(float dipValue) {
        return (int) (dipValue * 2);
    }

    /**
     * 将dip或dp值转换为px值
     *
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        if (context == null) {
            return 0;
        }
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }

    public static int px2dip(float pxValue) {
        return (int) (pxValue / 2 + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        if (context == null) {
            return 0;
        }
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 将px值转换为sp值
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取屏幕参数
     *
     * @param context
     */
    private static void getDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        DisplayWidthPixels = dm.widthPixels;// 宽度
        DisplayHeightPixels = dm.heightPixels;// 高度
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getDisplayWidthPixels(Context context) {
        if (context == null) {
            return -1;
        }
        if (DisplayWidthPixels == 0) {
            getDisplayMetrics(context);
        }
        return DisplayWidthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getDisplayHeightPixels(Context context) {
        if (context == null) {
            return -1;
        }
        if (DisplayHeightPixels == 0) {
            getDisplayMetrics(context);
        }
        return DisplayHeightPixels;
    }
}
