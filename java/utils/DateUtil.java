package com.qb.workstation.util;

import com.sun.istack.internal.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public final static String YYYYMMDDHHMMSS_PATTER = "yyyyMMddHHmmss";

    public static Date now() {
        return new Date();
    }

    /**
     * 获取指定日期当周星期一的时间
     *
     * @param date
     * @return
     */
    public static Date getSpecifyDateWeekStart(@NotNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //获取本周第一天
        calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR));
        int dayOfWeek = 0;
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
            dayOfWeek = -6;
        } else {
            dayOfWeek = 2 - calendar.get(Calendar.DAY_OF_WEEK);
        }
        calendar.add(Calendar.DAY_OF_WEEK, dayOfWeek);

        return calendar.getTime();
    }

    /**
     * 获取指定日期当周星期日的时间
     *
     * @param date
     * @return
     */
    public static Date getSpecifyDateWeekEnd(@NotNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() + 6); // Sunday

        return calendar.getTime();
    }

    /**
     * 获取指定日期当月第一天的时间
     *
     * @param date
     * @return
     */
    public static Date getSpecifyDateMonthStart(@NotNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * 获取指定日期当月最后一天的时间
     *
     * @param date
     * @return
     */
    public static Date getSpecifyDateMonthEnd(@NotNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 获取指定日期当年第一天的时间
     *
     * @param date
     * @return
     */
    public static Date getSpecifyDateYearStart(@NotNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, date.getYear() + 1900);
        return calendar.getTime();
    }

    /**
     * 获取指定日期当年最后一天的时间
     *
     * @param date
     * @return
     */
    public static Date getSpecifyDateYearEnd(@NotNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, date.getYear() + 1900);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }


    /**
     * 获取两个日期相隔的天数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static float getDateDiffOfDays(@NotNull Date startDate, @NotNull Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();

        return diff / (24 * 60 * 60 * 1000);
    }

    /**
     * 获取每天工作小时数。例如：早上9点上班，下午6点下班，休息一个小时，工作时间为8
     *
     * @param startTime 开始时间，格式：HHmm。例如早上7点半，使用0730表示
     * @param endTime   结束时间，格式：HHmm。例如下午6点半，使用1830表示
     * @param restHours 休息时间
     * @return
     */
    public static float getWorkHours(@NotNull String startTime, @NotNull String endTime, float restHours) {
        Date m = new Date();
        m.setHours(Integer.parseInt(startTime.substring(0, 2)) - 1);
        m.setMinutes(Integer.parseInt(startTime.substring(2)));
        Date a = new Date();
        a.setHours(Integer.parseInt(endTime.substring(0, 2)) - 1);
        a.setMinutes(Integer.parseInt(endTime.substring(2)));

        return (a.getTime() - m.getTime()) / (1000 * 60 * 60 * 1.0f) - restHours;
    }

    /**
     * 将日期字符串转换为日期类型
     *
     * @param dateStr 日期字符串
     * @param pattern 日期字符串格式
     * @return
     * @throws ParseException
     */
    public static Date parse(@NotNull String dateStr, @NotNull String pattern) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.parse(dateStr);
    }

    public static String formatToAnotherPattern(@NotNull String dateStr, @NotNull String pattern,@NotNull String anotherPatter) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        Date date = dateFormat.parse(dateStr);
        dateFormat = new SimpleDateFormat(anotherPatter);
        return dateFormat.format(date);
    }

    public static String format(Date date,String pattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    public static Date addHours(@NotNull Date date, int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    public static Date addDays(@NotNull Date date, int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }


    private static Date add(@NotNull Date date, int calendarField, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }
}
