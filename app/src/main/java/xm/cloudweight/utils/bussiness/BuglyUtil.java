package xm.cloudweight.utils.bussiness;

import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author wyh
 * @Description:
 * @creat 2017/12/5
 */
public class BuglyUtil {

    public static void uploadCrash(String message) {
        CrashReport.postCatchedException(new Exception(!TextUtils.isEmpty(message) ? message : "无data"));
    }

    public static void uploadCrash(Throwable thw) {
        CrashReport.postCatchedException(thw);
    }

}
