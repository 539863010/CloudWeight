package xm.cloudweight.utils.bussiness.printer;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.widget.Toast;

import com.citizen.sdk.labelprint.LabelConst;
import com.citizen.sdk.labelprint.LabelDesign;
import com.citizen.sdk.labelprint.LabelPrinter;

import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.ToastUtil;

/**
 * @author wyh
 * @Description: 打印库存标签
 * @creat 2017/11/1
 */
public class PrinterInventory {

    public static void printer(Context context, PrinterBean bean) {
        // Constructor
        LabelPrinter printer = new LabelPrinter();
        // Set context
        printer.setContext(context);
        // Get Address
        UsbDevice usbDevice = null;
        // Connect
        int result = printer.connect(LabelConst.CLS_PORT_USB, usbDevice);       // Android 3.1 ( API Level 12 ) or later
        if (LabelConst.CLS_SUCCESS == result) {
            // Print data output
            print(context, printer, bean);
            // Disconnect
            printer.disconnect();
        } else {
            // Connect Error
            //            Toast.makeText(context, "Connect or Printer Error : " + Integer.toString(result), Toast.LENGTH_LONG).show();
            ToastUtil.showShortToast(context, "打印机连接失败");
        }
    }

    private static void print(Context context, LabelPrinter printer, PrinterBean bean) {
        LabelDesign design = new LabelDesign();
        int result;
        String errmsg = "";

        int printCount = bean.getPrintCount();
        String goodsName = bean.getGoodsName();
        String qrcode = bean.getCode();

        // QRCode
        if (!TextUtils.isEmpty(qrcode)) {
            result = design.drawQRCode(qrcode,
                    LabelConst.CLS_ENC_CDPG_US_ASCII,
                    LabelConst.CLS_RT_NORMAL,
                    5,
                    LabelConst.CLS_QRCODE_EC_LEVEL_H,
                    20,
                    20);
            if (LabelConst.CLS_SUCCESS != result) {
                errmsg = errmsg.concat("二维码  ");
            }
        }

        String month = DateUtils.getCurrentMonth().concat("月");
        result = design.drawTextLocalFont(month,
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                135,
                135,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                105,
                115);
        if (LabelConst.CLS_SUCCESS != result) {
            errmsg = errmsg.concat("年份  ");
        }

        String day = DateUtils.getCurrentDay().concat("日");
        result = design.drawTextLocalFont(day,
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                135,
                135,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                105,
                95);
        if (LabelConst.CLS_SUCCESS != result) {
            errmsg = errmsg.concat("月份  ");
        }

        if (!TextUtils.isEmpty(qrcode)
                && qrcode.contains("-")
                && qrcode.split("-").length == 3
                && !TextUtils.isEmpty(qrcode.split("-")[2])) {
            //去0  001 ->  1
            int num = Integer.parseInt(qrcode.split("-")[2]);
            result = design.drawTextLocalFont(String.valueOf(num),
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    300,
                    300,
                    10,
                    (LabelConst.CLS_FNT_BOLD),
                    160,
                    90);
            if (LabelConst.CLS_SUCCESS != result) {
                errmsg = errmsg.concat("编号  ");
            }
        }

        if (!TextUtils.isEmpty(goodsName)) {
            if (goodsName.length() > 5) {
                String goodsNameStart = goodsName.substring(0, 5);
                String goodsNameEnd = goodsName.substring(5, goodsName.length());
                result = design.drawTextLocalFont(goodsNameStart,
                        Typeface.SERIF,
                        LabelConst.CLS_RT_NORMAL,
                        140,
                        140,
                        10,
                        (LabelConst.CLS_FNT_BOLD),
                        102,
                        46);
                if (LabelConst.CLS_SUCCESS != result) {
                    errmsg = errmsg.concat("商品名  ");
                }
                result = design.drawTextLocalFont(goodsNameEnd,
                        Typeface.SERIF,
                        LabelConst.CLS_RT_NORMAL,
                        140,
                        140,
                        10,
                        (LabelConst.CLS_FNT_BOLD),
                        105,
                        25);
                if (LabelConst.CLS_SUCCESS != result) {
                    errmsg = errmsg.concat("商品名  ");
                }
            } else {
                result = design.drawTextLocalFont(goodsName,
                        Typeface.SERIF,
                        LabelConst.CLS_RT_NORMAL,
                        140,
                        140,
                        10,
                        (LabelConst.CLS_FNT_BOLD),
                        105,
                        35);
                if (LabelConst.CLS_SUCCESS != result) {
                    errmsg = errmsg.concat("商品名  ");
                }
            }
        }
        // 设置热敏打印模式
        printer.setPrintMethod(LabelConst.CLS_PRTMETHOD_DT);
        // Set Property (Tear Off)   显示完整标签
        printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_TEAROFF);
        // Set Property (Measurement unit)
        printer.setMeasurementUnit(LabelConst.CLS_UNIT_INCH);
        // Print    1代表印刷数
        result = printer.print(design, printCount);
        if (LabelConst.CLS_SUCCESS != result) {
            errmsg = errmsg.concat("打印  ");
        }

        // Err Message
        if (!TextUtils.isEmpty(errmsg)) {
            Toast.makeText(context, "打印有误：" + errmsg, Toast.LENGTH_LONG).show();
        }

    }

}
