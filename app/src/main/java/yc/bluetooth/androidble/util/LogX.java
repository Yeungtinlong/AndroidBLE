package yc.bluetooth.androidble.util;

import android.util.Log;

public class LogX {
    public static final boolean openLog = true;

    public static void d(String tag, String message) {
        if (!openLog)
            return;

        Log.d(tag, message);
    }

    public static void e(String tag, String message) {
        if (!openLog)
            return;

        Log.e(tag, message);
    }

    public static void i(String tag, String message) {
        if (!openLog)
            return;

        Log.i(tag, message);
    }

    public static void w(String tag, String message) {
        if (!openLog)
            return;

        Log.w(tag, message);
    }
}
