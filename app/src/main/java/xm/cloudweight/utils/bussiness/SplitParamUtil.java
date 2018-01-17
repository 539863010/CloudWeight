package xm.cloudweight.utils.bussiness;

import android.text.TextUtils;

/**
 * @author wyh
 * @Description:
 * @creat 2018/1/17
 */
public class SplitParamUtil {

    private static final String SPLIT_TAG = "@";

    public static String concat(Object... params) {
        String p = "";
        int length = params.length;
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                p = p.concat(String.valueOf(params[i]));
            } else {
                p = p.concat(String.valueOf(params[i])).concat(SPLIT_TAG);
            }
        }
        return p;
    }

    public static String[] split(String params) {
        if (!TextUtils.isEmpty(params) && params.contains(SPLIT_TAG)) {
            return params.split(SPLIT_TAG);
        }
        return null;
    }

}
