package xm.cloudweight.utils;

import android.content.Context;
import android.widget.Toast;


/**
 * 显示toast工具类
 *
 * @author ssy
 */
public class ToastUtil {
    private static Toast toast;

    public static void showShortToast(Context context, String message) {
        if (context != null) {
            if (toast == null)
                toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
            else
                toast.setText(message);
            toast.show();
        }
    }

    public static void showLongToast(Context context, String message) {
        if (toast == null)
            toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG);
        else
            toast.setText(message);
        toast.show();
    }
//
//    public static void showFailedMsg(Context context, Object failedMsg, String message) {
//        if (toast == null)
//            toast = Toast.makeText(context.getApplicationContext(), failedMsg == null ? message : failedMsg + "", Toast.LENGTH_LONG);
//        else
//            toast.setText(failedMsg == null ? message : failedMsg + "");
//        toast.show();
//    }
//
//    public static void showErrorMessage(Context context, String message) {
//        if (toast == null)
//            toast = Toast.makeText(context.getApplicationContext(), message + LocalGlobalError.SERVER_UNAVAILABLE, Toast.LENGTH_LONG);
//        else
//            toast.setText(message + LocalGlobalError.SERVER_UNAVAILABLE);
//        toast.show();
//    }
}
