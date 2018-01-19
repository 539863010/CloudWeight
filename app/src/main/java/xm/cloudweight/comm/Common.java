package xm.cloudweight.comm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author wyh
 * @Description: 常量类
 * @creat 2017/10/26
 */
public class Common {

    public static final String PLAT_FORM = "scale";
    public static final String USER_IDENTITY = "merchant";
    public static final String MACHINE_NUM = "001";

    public interface DbType {
        /**
         * 验收-入库
         */
        int TYPE_ChECK_IN_STORE_IN = 1;
        String STR_TYPE_ChECK_IN_STORE_IN = "入库";
        /**
         * 验收-越库
         */
        int TYPE_ChECK_IN_CROSS_OUT = 2;
        String STR_TYPE_ChECK_IN_CROSS_OUT = "越库";
        /**
         * 验收-越库调拨
         */
        int TYPE_ChECK_IN_CROSS_ALLCOCATE = 7;
        String STR_TYPE_ChECK_IN_CROSS_ALLCOCATE = "越库调拨";
        /**
         * 分拣-分拣
         */
        int TYPE_SORT_OUT_STORE_OUT = 3;
        /**
         * 出库
         */
        int TYPE_STORE_OUT = 4;
        String STR_TYPE_STORE_OUT = "出库";
        /**
         * 调拨
         */
        int TYPE_ALLOCATE = 5;
        String STR_TYPE_ALLOCATE = "调拨";
        /**
         * 盘点
         */
        int TYPE_CHECK = 6;
    }

    /**
     * 出库
     */
    public static final int SIMILAR_STOCKOUT = 1;
    /**
     * 调拨
     */
    public static final int SIMILAR_ALLOCATE = 2;
    /**
     * 盘点
     */
    public static final int SIMILAR_CHECK = 3;

    /**
     * 周转筐长度
     */
    public static final int BasketLength = 6;

    /**
     * 溯源码
     */
    public static String getPlatformTraceCode() {
        try {
            SimpleDateFormat dateformat1 = new SimpleDateFormat("yyMMddHHmmssSSS", Locale.getDefault());
            String format = dateformat1.format(new Date());
            return Common.MACHINE_NUM.concat(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
