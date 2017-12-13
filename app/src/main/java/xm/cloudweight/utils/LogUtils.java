package xm.cloudweight.utils;

import android.util.Log;

import xm.cloudweight.BuildConfig;

/**
 * 日志工具类
 *
 * @author ssy
 */
public class LogUtils {
    private static final boolean Debug = BuildConfig.DEBUG;
    private static final String TAG = "易家患者";
    private static int LOG_MAXLENGTH = 3500;

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
            int strLength = msg.length();
            int start = 0;
            int end = LOG_MAXLENGTH;
            for (int i = 0; i < 100; i++) {
                if (strLength > end) {
                    Log.e(tag + i, msg.substring(start, end));
                    start = end;
                    end = end + LOG_MAXLENGTH;
                } else {
                    Log.e(tag + i, msg.substring(start, strLength));
                    break;
                }
            }
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
