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
 * @author wyh
 * @Description: 打印分拣标签
 * @creat 2017/11/1
 */
public class PrinterSortOut {

    public static final String SORT_OUT_QRCODE = "http://trace.lbh.atfresh.cn/h5/#/index/";

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

    //
    // print
    //
    private static void print(Context context, LabelPrinter printer, PrinterBean bean) {
        LabelDesign design = new LabelDesign();
        int result;
        String errmsg = "";

        int printCount = bean.getPrintCount();
        String code = bean.getCode();
        String customer = bean.getCustomer();
        String department = bean.getDepartment();
        String goodsName = bean.getGoodsName();
        String sortOutNum = bean.getNum();
        String storageMode = bean.getStorageMode();
        String period = bean.getPeriod();

        // QRCode
        if (!TextUtils.isEmpty(code)) {
            design.drawQRCode(SORT_OUT_QRCODE.concat(code),
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
        if (!TextUtils.isEmpty(department)) {
            title = title.concat(department);
        }
        if (!TextUtils.isEmpty(customer)) {
            if (!TextUtils.isEmpty(title)) {
                title = title.concat("-").concat(customer);
            } else {
                title = title.concat(customer);
            }
        }
        if (!TextUtils.isEmpty(title)) {
            design.drawTextLocalFont(title,
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    95,
                    95,
                    10,
                    (LabelConst.CLS_FNT_BOLD),
                    95,
                    91);
        }
        if (!(TextUtils.isEmpty(storageMode) && TextUtils.isEmpty(period))) {
            storageMode = !TextUtils.isEmpty(bean.getStorageMode()) ? bean.getStorageMode() : "无";
            period = !TextUtils.isEmpty(bean.getPeriod()) ? bean.getPeriod() : "无";
            design.drawTextLocalFont("保存条件:".concat(storageMode),
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    60,
                    60,
                    10,
                    (LabelConst.CLS_FNT_DEFAULT),
                    95,
                    75);

            design.drawTextLocalFont("保质期:".concat(period),
                    Typeface.SERIF,
                    LabelConst.CLS_RT_NORMAL,
                    60,
                    60,
                    10,
                    (LabelConst.CLS_FNT_DEFAULT),
                    95,
                    65);
        }

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
