package ru.noties.ccf.sample;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.Window;

/**
 * Created by Dimitry Ivanov on 06.11.2015.
 */
public abstract class StatusBarColor {

    public static StatusBarColor newInstance(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new StatusBarColorLollipop(window);
        }
        return new StatusBarColorImpl();
    }

    public abstract void setStatusBarColor(@ColorInt int color);

    private static class StatusBarColorImpl extends StatusBarColor {

        @Override
        public void setStatusBarColor(@ColorInt int color) {
            // do nothing
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class StatusBarColorLollipop extends StatusBarColor {

        private final Window mWindow;

        StatusBarColorLollipop(Window window) {
            mWindow = window;
        }

        @Override
        public void setStatusBarColor(@ColorInt int color) {
            mWindow.setStatusBarColor(color);
        }
    }
}
