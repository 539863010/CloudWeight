package xm.cloudweight.utils.bussiness;

import android.text.TextUtils;

/**
 * @author wyh
 * @Description: 获取文件名
 * @creat 2018/1/11
 */
public class GetImageFile {

    public static String getName(String imagePath) {
        String name = "";
        if (!TextUtils.isEmpty(imagePath) && imagePath.contains("/")) {
            name = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
        }
        return name;
    }

}
