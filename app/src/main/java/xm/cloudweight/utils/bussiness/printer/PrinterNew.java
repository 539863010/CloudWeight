package xm.cloudweight.utils.bussiness.printer;

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
 * @author : wyh
 * @create : 2018/2/27
 * @des :  新格式标签
 */
public class PrinterNew {

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
                    30);
            //            if (LabelConst.CLS_SUCCESS != result) {
            //                errmsg = errmsg.concat("二维码  ");
            //            }
        }

        if (!TextUtils.isEmpty(goodsName)) {
            if (goodsName.length() > 6) {
                goodsName = goodsName.substring(0, 7);
            }
            design.drawTextLocalFont(goodsName,
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    105,
                    105,
                    10,
                    (LabelConst.CLS_FNT_BOLD),
                    95,
                    123);
        }

        if (!TextUtils.isEmpty(sortOutNum)) {
            design.drawTextLocalFont("数量:".concat(sortOutNum),
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    105,
                    105,
                    10,
                    (LabelConst.CLS_FNT_BOLD),
                    95,
                    107);
        }

        String title = "";
        if (!TextUtils.isEmpty(customer)) {
            title = customer;
        }
        if (!TextUtils.isEmpty(department)) {
            title = title.concat("【").concat(department).concat("】");
        }
        if (!TextUtils.isEmpty(title)) {
            design.drawTextLocalFont(title,
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    105,
                    105,
                    10,
                    (LabelConst.CLS_FNT_BOLD),
                    95,
                    91);
        }

        design.drawTextLocalFont("保存条件:",
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                95,
                75);

        design.drawTextLocalFont("保质期:",
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                95,
                65);

        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
        String date = "包装日期:".concat(dateformat.format(new Date()));
        design.drawTextLocalFont(date,
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                95,
                55);

        design.drawTextLocalFont("追溯码:".concat(code),
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                95,
                45);

        design.drawTextLocalFont("手机扫描二维码查询追溯信息",
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                95,
                35);

        design.drawTextLocalFont("加工单位：厦门绿百合食品有限公司",
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                45,
                18);

        design.drawTextLocalFont("地址：厦门湖里区渔港西路10号3楼",
                Typeface.SERIF,
                LabelConst.CLS_RT_NORMAL,
                60,
                60,
                10,
                (LabelConst.CLS_FNT_DEFAULT),
                45,
                8);

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