package xm.cloudweight.utils.bussiness;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.widget.Toast;

import com.citizen.sdk.labelprint.LabelConst;
import com.citizen.sdk.labelprint.LabelDesign;
import com.citizen.sdk.labelprint.LabelPrinter;

import java.text.SimpleDateFormat;
import java.util.Date;

import xm.cloudweight.utils.ToastUtil;

/**
 * @author wyh
 * @Description: 打印分拣标签
 * @creat 2017/11/1
 */
public class PrinterSortOut {

    public static final String SORT_OUT_QRCODE = "http://trace.lbh.atfresh.cn/h5/#/index/";

    public static void printer(Context context, int printCount, String qrcodeContent, String customer, String department, String goodsName, String sortOutNum, String code) {
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
            print(context, printer, printCount, qrcodeContent, customer, department, goodsName, sortOutNum, code);
            // Disconnect
            printer.disconnect();
        } else {
            // Connect Error
//            Toast.makeText(context, "Connect or Printer Error : " + Integer.toString(result), Toast.LENGTH_LONG).show();
            ToastUtil.showShortToast(context, "打印机连接失败");
        }
    }

    //
    // print
    //
    private static void print(Context context, LabelPrinter printer, int printCount, String qrcodeContent, String customer, String department, String goodsName, String sortOutNum, String code) {
        LabelDesign design = new LabelDesign();
        int result;
        String errmsg = "";

        // QRCode
        if (!TextUtils.isEmpty(qrcodeContent)) {
            result = design.drawQRCode(qrcodeContent,
                    LabelConst.CLS_ENC_CDPG_US_ASCII,
                    LabelConst.CLS_RT_NORMAL,
                    3,
                    LabelConst.CLS_QRCODE_EC_LEVEL_H,
                    20,
                    18);
            if (LabelConst.CLS_SUCCESS != result) {
                errmsg = errmsg.concat("二维码  ");
            }
        }
        String title = "";
        if (!TextUtils.isEmpty(customer)) {
            title = customer;
        }
        if (!TextUtils.isEmpty(department)) {
            title = title.concat("【").concat(department).concat("】");
        }
        if (!TextUtils.isEmpty(title)) {
            if (title.length() > 6) {
                String titleFirst = title.substring(0, 6);
                String titleSecond = title.substring(6, title.length());
                design.drawTextLocalFont(titleFirst,
                        Typeface.SERIF,
                        LabelConst.CLS_RT_NORMAL,
                        120,
                        120,
                        10,
                        (LabelConst.CLS_FNT_BOLD),
                        95,
                        115);

                design.drawTextLocalFont(titleSecond,
                        Typeface.SERIF,
                        LabelConst.CLS_RT_NORMAL,
                        120,
                        120,
                        10,
                        (LabelConst.CLS_FNT_BOLD),
                        105,
                        95);
            } else {
                design.drawTextLocalFont(title,
                        Typeface.SERIF,
                        LabelConst.CLS_RT_NORMAL,
                        120,
                        120,
                        10,
                        (LabelConst.CLS_FNT_BOLD),
                        95,
                        115);
            }
        }

        if (!TextUtils.isEmpty(goodsName)) {
            if (goodsName.length() > 6) {
                goodsName = goodsName.substring(0, 7);
            }
            result = design.drawTextLocalFont(goodsName,
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    105,
                    105,
                    10,
                    (LabelConst.CLS_FNT_BOLD),
                    105,
                    65);
            if (LabelConst.CLS_SUCCESS != result) {
                errmsg = errmsg.concat("商品名  ");
            }
        }

        if (!TextUtils.isEmpty(sortOutNum)) {
            result = design.drawTextLocalFont(sortOutNum,
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    105,
                    105,
                    10,
                    (LabelConst.CLS_FNT_DEFAULT),
                    120,
                    48);
            if (LabelConst.CLS_SUCCESS != result) {
                errmsg = errmsg.concat("分拣数  ");
            }
        }

        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
        String date = "包装日期:".concat(dateformat.format(new Date()));
        result = design.drawTextLocalFont(date,
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                112,
                35);
        if (LabelConst.CLS_SUCCESS != result) {
            errmsg = errmsg.concat("包装日期  ");
        }

        result = design.drawTextLocalFont("追溯码:".concat(code),
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                95,
                25);
        if (LabelConst.CLS_SUCCESS != result) {
            errmsg = errmsg.concat("追溯码  ");
        }

        design.drawTextLocalFont("手机扫描二维码查询追溯信息",
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                97,
                15);

        // 设置热敏打印模式
        printer.setPrintMethod(LabelConst.CLS_PRTMETHOD_DT);
        // Set Property (Tear Off)   显示完整标签
        printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_TEAROFF);
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
