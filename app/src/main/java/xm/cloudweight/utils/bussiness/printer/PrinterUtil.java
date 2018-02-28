package xm.cloudweight.utils.bussiness.printer;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.widget.Toast;

import com.citizen.sdk.labelprint.LabelConst;
import com.citizen.sdk.labelprint.LabelDesign;
import com.citizen.sdk.labelprint.LabelPrinter;

/**
 * @author wyh
 * @Description: 打印机Util
 * @creat 2017/11/1
 */
@Deprecated
public class PrinterUtil {

    public static void printer(Context context, String filePath, int printCount) {
        // Constructor
        LabelPrinter printer = new LabelPrinter();

        // Set context
        printer.setContext(context);

        // Get Address
        UsbDevice usbDevice = null;                                               // null (Automatic detection)

        // Connect
        int result = printer.connect(LabelConst.CLS_PORT_USB, usbDevice);       // Android 3.1 ( API Level 12 ) or later
        if (LabelConst.CLS_SUCCESS == result) {
            // Print data output
            print(context, printer, filePath, printCount);

            // Disconnect
            printer.disconnect();
        } else {
            // Connect Error
            Toast.makeText(context, "Connect or Printer Error : " + Integer.toString(result), Toast.LENGTH_LONG).show();
        }
    }

    //
    // print
    //
    private static void print(Context context, LabelPrinter printer, String filePath, int printCount) {
        LabelDesign design = new LabelDesign();
        int errflg = 0;
        int result;
        String errmsg = "";


        result =  design.drawBitmap(filePath ,
                LabelConst.CLS_RT_NORMAL, 0, 0, 10, 0,
                LabelConst.CLS_PRT_RES_300, LabelConst.CLS_UNIT_MILLI);

//        result =  design.drawBitmap(filePath ,
//                LabelConst.CLS_RT_NORMAL, 0, 0, 10, 5);


        if (LabelConst.CLS_SUCCESS != result) {
            errmsg = errmsg + Integer.toString(result) + ":\tdrawTextPtrFont Error\r\n";
            errflg = 1;
        }

        // QRCode
//        result = design.drawQRCode("DrawQRCode", LabelConst.CLS_ENC_CDPG_US_ASCII, LabelConst.CLS_RT_NORMAL, 4, LabelConst.CLS_QRCODE_EC_LEVEL_H, 20, 220);
//        if (LabelConst.CLS_SUCCESS != result) {
//            errmsg = errmsg + Integer.toString(result) + ":\tdrawQRCode Error\r\n";
//            errflg = 1;
//        }

        // Rect(fill)
//        result = design.fillRect(20, 150, 350, 40, LabelConst.CLS_SHADED_PTN_11);
//        if (LabelConst.CLS_SUCCESS != result)
//        {
//            errmsg = errmsg + Integer.toString(result) + ":\tfillRect Error\r\n";
//            errflg = 1;
//        }

        // BarCode
//        result = design.drawBarCode("0123456789", LabelConst.CLS_BCS_CODE128, LabelConst.CLS_RT_NORMAL, 3, 3, 30, 20, 70, LabelConst.CLS_BCS_TEXT_SHOW);
//        if (LabelConst.CLS_SUCCESS != result) {
//            errmsg = errmsg + Integer.toString(result) + ":\tdrawBarCode Error\r\n";
//            errflg = 1;
//        }

        // 设置热敏打印模式
        printer.setPrintMethod(LabelConst.CLS_PRTMETHOD_DT);
        // Set Property (Tear Off)   显示完整标签
        printer.setMediaHandling(LabelConst.CLS_MEDIAHANDLING_TEAROFF);
        // Print    1代表印刷数
        result = printer.print(design, printCount);
        if (LabelConst.CLS_SUCCESS != result) {
            errmsg = errmsg + Integer.toString(result) + ":\tprint Error";
            errflg = 1;
        }

        // Err Message
        if (errflg != 0) {
            Toast.makeText(context, errmsg, Toast.LENGTH_LONG).show();
        }

    }

}
