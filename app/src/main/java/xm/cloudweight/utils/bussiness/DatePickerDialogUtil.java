package xm.cloudweight.utils.bussiness;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.Date;

import xm.cloudweight.bean.DateEntity;
import xm.cloudweight.utils.DateUtils;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/7
 */
public class DatePickerDialogUtil {

    public static void show(Context ctx, String dateStr, DatePickerDialog.OnDateSetListener listener) {
        DateEntity begin = new DateEntity();
        if (TextUtils.isEmpty(dateStr)) {
            Calendar c = Calendar.getInstance();
            begin.setYear(c.get(Calendar.YEAR));
            begin.setMonth(c.get(Calendar.MONTH));
            begin.setDay(c.get(Calendar.DAY_OF_MONTH));
        } else {
            Date date = DateUtils.converToDate2(dateStr);
            Calendar c = Calendar.getInstance();
            if (date != null) {
                c.setTime(date);
            }
            begin.setYear(c.get(Calendar.YEAR));
            begin.setMonth(c.get(Calendar.MONTH));
            begin.setDay(c.get(Calendar.DAY_OF_MONTH));
        }
        DatePickerDialog dateDialog = new DatePickerDialog(ctx,
                listener,
                // 传入年份
                begin.getYear(),
                // 传入月份
                begin.getMonth(),
                // 传入天数
                begin.getDay()
        );
        dateDialog.show();
    }
}
