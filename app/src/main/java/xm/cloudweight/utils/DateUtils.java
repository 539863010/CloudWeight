package xm.cloudweight.utils;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DateUtils {

    private static String mYear; // 当前年  
    private static String mMonth; // 月  
    private static String mDay;
    private static String mWay;

    public static String getTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd");
        return dateformat1.format(date);
    }

    public static String getTime2(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateformat1.format(date);
    }

    //把日期转为字符串
    public static String converToString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    public static Date converToDate(String strDate) {
        if (TextUtils.isEmpty(strDate)) {
            return null;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date converToDate2(String strDate) {
        if (TextUtils.isEmpty(strDate)) {
            return null;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前日期几月几号
     */
    public static String getDateString() {

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份  
        mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码  
        return mMonth + "月" + mDay + "日";
    }

    /**
     * 获取当前年月日
     *
     * @return
     */
    public static String StringData() {

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mYear = String.valueOf(c.get(Calendar.YEAR));// 获取当前年份  
        mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份  
        mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码  
        return mYear + "-" + mMonth + "-" + mDay;
    }

    /**
     * 获取当前是周几
     */
    public static String getWeekString() {
        final Calendar c = Calendar.getInstance();
        mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "周天";
        } else if ("2".equals(mWay)) {
            mWay = "周一";
        } else if ("3".equals(mWay)) {
            mWay = "周二";
        } else if ("4".equals(mWay)) {
            mWay = "周三";
        } else if ("5".equals(mWay)) {
            mWay = "周四";
        } else if ("6".equals(mWay)) {
            mWay = "周五";
        } else if ("7".equals(mWay)) {
            mWay = "周六";
        }
        return mDay;
    }

    /**
     * 根据当前日期获得是星期几
     *
     * @return
     */
    public static String getWeek(String time) {
        String Week = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {

            c.setTime(format.parse(time));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            Week += "周天";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            Week += "周一";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            Week += "周二";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            Week += "周三";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            Week += "周四";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            Week += "周五";
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            Week += "周六";
        }
        return Week;
    }

    /**
     * 获取今天往后一周的日期（年-月-日）
     */
    public static List<String> get7date() {
        List<String> dates = new ArrayList<String>();
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        java.text.SimpleDateFormat sim = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String date = sim.format(c.getTime());
        dates.add(date);
        for (int i = 0; i < 6; i++) {
            c.add(java.util.Calendar.DAY_OF_MONTH, 1);
            date = sim.format(c.getTime());
            dates.add(date);
        }
        return dates;
    }

    /**
     * 获取今天往后一周的日期（几月几号）
     */
    public static List<String> getSevendate() {
        List<String> dates = new ArrayList<String>();
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

        for (int i = 0; i < 7; i++) {
            mYear = String.valueOf(c.get(Calendar.YEAR));// 获取当前年份
            mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
            mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + i);// 获取当前日份的日期号码
            String date = mMonth + "月" + mDay + "日";
            dates.add(date);
        }
        return dates;
    }

    /**
     * 获取今天往后一周的集合
     */
    public static List<String> get7week() {
        String week = "";
        List<String> weeksList = new ArrayList<String>();
        List<String> dateList = get7date();
        for (String s : dateList) {
            if (s.equals(StringData())) {
                week = "今天";
            } else {
                week = getWeek(s);
            }
            weeksList.add(week);
        }
        return weeksList;
    }

}
