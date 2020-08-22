package com.xgimi.filemanager.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/6/24 12:49
 * 代码如下：
 * 屏幕适配全局初始化
 * ScreenAdapterUtils.setup(this)
 * ScreenAdapterUtils.register(this, 1280F, ScreenAdapterUtils.MATCH_BASE_WIDTH, ScreenAdapterUtils.MATCH_UNIT_DP)
 */
public class ScreenAdapterUtils {

    private static final String TAG = "ScreenAdapterUtils";

    /**
     * 屏幕适配的基准
     */
    public static final int MATCH_BASE_WIDTH = 0;
    public static final int MATCH_BASE_HEIGHT = 1;
    /**
     * 适配单位
     */
    public static final int MATCH_UNIT_DP = 0;
    public static final int MATCH_UNIT_PT = 1;

    // 适配信息
    private static MatchInfo sMatchInfo = null;

    // Activity 的生命周期监测
    private static Application.ActivityLifecycleCallbacks mActivityLifecycleCallback = null;

    /**
     * 初始化
     *
     * @param application
     */
    public static void setup(@NonNull Application application) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        if (sMatchInfo == null) { // 记录系统的原始值
            sMatchInfo = new MatchInfo();
            sMatchInfo.screenWidth = displayMetrics.widthPixels;
            sMatchInfo.screenHeight = displayMetrics.heightPixels;
            sMatchInfo.appDensity = displayMetrics.density;
            sMatchInfo.appDensityDpi = displayMetrics.densityDpi;
            sMatchInfo.appScaledDensity = displayMetrics.scaledDensity;
            sMatchInfo.appXdpi = displayMetrics.xdpi;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { // 添加字体变化的监听
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration configuration) {
                    if (configuration != null && configuration.fontScale > 0) {
                        sMatchInfo.appScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }
    }

    /**
     * 在 application 中全局激活适配（也可单独使用 match() 方法在指定页面中配置适配）
     */
    public static void register(@NonNull Application application, float designSize, int matchBase, int matchUnit) {
        setup(application);
        if (mActivityLifecycleCallback == null) {
            mActivityLifecycleCallback = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle bundle) {
                    match(activity, designSize, matchBase, matchUnit);
                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {

                }
            };
            application.registerActivityLifecycleCallbacks(mActivityLifecycleCallback);
        }
    }

    /**
     * 全局取消所有的适配
     */
    public void unregister(@NonNull Application application, @NonNull int matchUnit) {
        if (mActivityLifecycleCallback != null) {
            application.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback);
            mActivityLifecycleCallback = null;
        }
        for (int i = 0; i < matchUnit; i++) {
            cancelMatch(application, i);
        }
    }


    /**
     * 适配屏幕（放在 Activity 的 setContentView() 之前执行）
     *
     * @param context
     * @param designSize
     */
    public void match(@NonNull Context context, float designSize) {
        match(context, designSize, MATCH_BASE_WIDTH, MATCH_UNIT_DP);
    }

    /**
     * 适配屏幕（放在 Activity 的 setContentView() 之前执行）
     *
     * @param context
     * @param designSize
     * @param matchBase
     */
    public void match(
            @NonNull Context context, float designSize,
            int matchBase
    ) {
        match(context, designSize, matchBase, MATCH_UNIT_DP);
    }

    /**
     * 适配屏幕（放在 Activity 的 setContentView() 之前执行）
     *
     * @param context
     * @param designSize 设计图的尺寸
     * @param matchBase  适配基准
     * @param matchUnit  使用的适配单位
     */
    public static void match(
            @NonNull Context context, float designSize,
            int matchBase,
            int matchUnit
    ) {
        if (designSize == 0f) {
            throw new UnsupportedOperationException("The designSize cannot be equal to 0");
        }
        if (matchUnit == MATCH_UNIT_DP) {
            matchByDP(context, designSize, matchBase);
        } else if (matchUnit == MATCH_UNIT_PT) {
            matchByPT(context, designSize, matchBase);
        }
    }

    /**
     * 重置适配信息，取消适配
     */
    public void cancelMatch(@NonNull Context context) {
        cancelMatch(context, MATCH_UNIT_DP);
        cancelMatch(context, MATCH_UNIT_PT);
    }

    /**
     * 重置适配信息，取消适配
     *
     * @param context
     * @param matchUnit 需要取消适配的单位
     */
    public void cancelMatch(@NonNull Context context, int matchUnit) {
        if (sMatchInfo != null) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            if (matchUnit == MATCH_UNIT_DP) {
                if (displayMetrics.density != sMatchInfo.appDensity) {
                    displayMetrics.density = sMatchInfo.appDensity;
                }
                if (displayMetrics.densityDpi != sMatchInfo.appDensityDpi) {
                    displayMetrics.densityDpi = (int) sMatchInfo.appDensityDpi;
                }
                if (displayMetrics.scaledDensity != sMatchInfo.appScaledDensity) {
                    displayMetrics.scaledDensity = sMatchInfo.appScaledDensity;
                }
            } else if (matchUnit == MATCH_UNIT_PT) {
                if (displayMetrics.xdpi != sMatchInfo.appXdpi) {
                    displayMetrics.xdpi = sMatchInfo.appXdpi;
                }
            }
        }
    }

    public MatchInfo getMatchInfo() {
        return sMatchInfo;
    }

    /**
     * 使用 dp 作为适配单位（适合在新项目中使用，在老项目中使用会对原来既有的 dp 值产生影响）
     * <p>
     * dp 与 px 之间的换算:
     * px = density(0.75) * dp(1280)
     * density(0.75) = dpi(120) / 160
     * px = dp * (dpi / 160)
     *
     * @param context
     * @param designSize 设计图的宽/高（单位: dp）
     * @param base       适配基准
     */
    private static void matchByDP(@NonNull Context context, float designSize, int base) {
        float targetDensity;
        if (base == MATCH_BASE_WIDTH) {
            targetDensity = sMatchInfo.screenWidth * 1f / designSize;
        } else if (base == MATCH_BASE_HEIGHT) {
            targetDensity = sMatchInfo.screenHeight * 1f / designSize;
        } else {
            targetDensity = sMatchInfo.screenWidth * 1f / designSize;
        }

        // 在Android中，规定以160dpi（即屏幕分辨率为320x480）为基准：1dp=1px
        int targetDensityDpi = (int) (targetDensity * 160);
        float targetScaledDensity = targetDensity * (sMatchInfo.appScaledDensity / sMatchInfo.appDensity); // 0.75
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        displayMetrics.density = targetDensity;
        displayMetrics.densityDpi = targetDensityDpi;
        displayMetrics.scaledDensity = targetScaledDensity;

        Log.d(TAG, "targetDensity:" + targetDensity);
        Log.d(TAG, "targetDensityDpi:" + targetDensityDpi);
        Log.d(TAG, "targetScaledDensity:" + targetScaledDensity);
    }

    /**
     * 使用 px 作为适配单位，专门为使用 px 单位的项目做适配
     *
     * @param context
     * @param designSize 设计图的宽/高（单位: px）
     * @param base       适配基准
     */
    private static void matchByPX(@NonNull Context context, float designSize, int base) {
        // 1920 1080
        // 960 540

        float targetDensity;
        if (base == MATCH_BASE_WIDTH) {
            // 1920 ---> 960
            targetDensity = designSize / sMatchInfo.screenWidth * 1f;
        } else if (base == MATCH_BASE_HEIGHT) {
            targetDensity = designSize / sMatchInfo.screenHeight * 1f;
        } else {
            targetDensity = designSize / sMatchInfo.screenWidth * 1f;
        }

        // 在Android中，规定以160dpi（即屏幕分辨率为320x480）为基准：1dp=1px
        int targetDensityDpi = (int) (targetDensity * 160);
        float targetScaledDensity = targetDensity * (sMatchInfo.appScaledDensity / sMatchInfo.appDensity); // 0.75
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        displayMetrics.density = targetDensity;
        displayMetrics.densityDpi = targetDensityDpi;
        displayMetrics.scaledDensity = targetScaledDensity;

        Log.d(TAG, "targetDensity:" + targetDensity);
        Log.d(TAG, "targetDensityDpi:" + targetDensityDpi);
        Log.d(TAG, "targetScaledDensity:" + targetScaledDensity);
    }

    /**
     * 使用 px 作为适配单位，专门为使用 px 单位的项目做适配
     * <br></br>
     * <p>
     * pt 转 px 算法: pt * metrics.xdpi * (1.0f/72)
     *
     * @param context
     * @param designSize 设计图的宽/高（单位: pt）
     * @param base       适配基准
     */
    private static void matchByPT(@NonNull Context context, float designSize, int base) {
        float targetXdpi;
        if (base == MATCH_BASE_WIDTH) {
            targetXdpi = sMatchInfo.screenWidth * 72f / designSize;
        } else if (base == MATCH_BASE_HEIGHT) {
            targetXdpi = sMatchInfo.screenHeight * 72f / designSize;
        } else {
            targetXdpi = sMatchInfo.screenWidth * 72f / designSize;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        displayMetrics.xdpi = targetXdpi;
    }

    /**
     * 适配信息
     */
    static class MatchInfo {
        private int screenWidth = 0;
        private int screenHeight = 0;
        private float appDensity = 0f;
        private float appDensityDpi = 0f;
        private float appScaledDensity = 0f;
        private float appXdpi = 0f;

        public int getScreenWidth() {
            return screenWidth;
        }

        public void setScreenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
        }

        public int getScreenHeight() {
            return screenHeight;
        }

        public void setScreenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
        }

        public float getAppDensity() {
            return appDensity;
        }

        public void setAppDensity(float appDensity) {
            this.appDensity = appDensity;
        }

        public float getAppDensityDpi() {
            return appDensityDpi;
        }

        public void setAppDensityDpi(float appDensityDpi) {
            this.appDensityDpi = appDensityDpi;
        }

        public float getAppScaledDensity() {
            return appScaledDensity;
        }

        public void setAppScaledDensity(float appScaledDensity) {
            this.appScaledDensity = appScaledDensity;
        }

        public float getAppXdpi() {
            return appXdpi;
        }

        public void setAppXdpi(float appXdpi) {
            this.appXdpi = appXdpi;
        }
    }

    public static double getScreenInch(Activity context) {
        double mInch = 0;
        try {
            int realWidth = 0, realHeight = 0;
            Display display = context.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            if (Build.VERSION.SDK_INT >= 17) {
                Point size = new Point();
                display.getRealSize(size);
                realWidth = size.x;
                realHeight = size.y;
            } else if (Build.VERSION.SDK_INT < 17
                    && Build.VERSION.SDK_INT >= 14) {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } else {
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }
            mInch = formatDouble(Math.sqrt(
                    (realWidth / metrics.xdpi) * (realWidth / metrics.xdpi) + (realHeight / metrics.ydpi) * (realHeight / metrics.ydpi)),
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mInch;
    }

    /**
     * Double类型保留指定位数的小数，返回double类型（四舍五入）
     * newScale 为指定的位数
     */
    private static double formatDouble(double d, int newScale) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}




