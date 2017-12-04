package xm.cloudweight.utils;

import android.util.Log;


/**
 * 日志工具类
 *
 * @author ssy
 */
public class LogUtils {
    private static final boolean Debug = true;
    private static final String TAG = "易家患者";

    public static void d(String tag, String msg) {
        if (Debug) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (Debug) {
            Log.d(tag, msg, tr);
        }
    }

    public static void err(Object msg) {
        if (Debug) {
            Log.e(TAG, "" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (Debug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (Debug) {
            Log.e(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (Debug) {
            Log.i(tag, msg);
        }
    }

}
