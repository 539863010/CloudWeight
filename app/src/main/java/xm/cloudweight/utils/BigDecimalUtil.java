/**
 * 版权所有©，厦门走云网络科技有限公司，2016，所有权利保留。
 * <p>
 * 项目名：	storm-common-api
 * 文件名：	BigDecimalUtil.java
 * 模块说明：
 * 修改历史：
 * 2016年5月27日 - subinzhu - 创建。
 */
package xm.cloudweight.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author wyh
 * @Description: BigDecimal工具类
 * @create 2017/11/15
 */
public class BigDecimalUtil {

    /**
     * 转换成n位小数格式（舍入模式为四舍五入）
     * @return 转换后结果
     */
    public static String toScaleStr(BigDecimal bigDecimal) {
        return String.valueOf(bigDecimal.setScale(2, RoundingMode.HALF_UP));
    }
}
