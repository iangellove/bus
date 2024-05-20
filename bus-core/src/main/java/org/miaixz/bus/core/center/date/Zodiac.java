/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.date;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.DateKit;

import java.util.Calendar;
import java.util.Date;

/**
 * 星座 来自：<a href="https://blog.csdn.net/u010758605/article/details/48317881">https://blog.csdn.net/u010758605/article/details/48317881</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Zodiac {

    /**
     * 星座分隔时间日
     */
    private static final int[] DAY_ARR = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};
    /**
     * 星座
     */
    private static final String[] ZODIACS = new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};
    private static final String[] CHINESE_ZODIACS = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};

    /**
     * 通过生日计算星座
     *
     * @param date 出生日期
     * @return 星座名
     */
    public static String getZodiac(final Date date) {
        return getZodiac(DateKit.calendar(date));
    }

    /**
     * 通过生日计算星座
     *
     * @param calendar 出生日期
     * @return 星座名
     */
    public static String getZodiac(final Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getZodiac(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数
     * @param day   天
     * @return 星座名
     */
    public static String getZodiac(final Fields.Month month, final int day) {
        return getZodiac(month.getValue(), day);
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数，见{@link Fields.Month#getValue()}
     * @param day   天
     * @return 星座名
     */
    public static String getZodiac(final int month, final int day) {
        Assert.checkBetween(month,
                Fields.Month.JANUARY.getValue(),
                Fields.Month.DECEMBER.getValue(),
                "Unsupported month value, must be [0,12]");
        // 在分隔日前为前一个星座，否则为后一个星座
        return day < DAY_ARR[month] ? ZODIACS[month] : ZODIACS[month + 1];
    }

    /**
     * 通过生日计算生肖，只计算1900年后出生的人
     *
     * @param date 出生日期（年需农历）
     * @return 星座名
     */
    public static String getChineseZodiac(final Date date) {
        return getChineseZodiac(DateKit.calendar(date));
    }

    /**
     * 通过生日计算生肖，只计算1900年后出生的人
     *
     * @param calendar 出生日期（年需农历）
     * @return 星座名
     */
    public static String getChineseZodiac(final Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getChineseZodiac(calendar.get(Calendar.YEAR));
    }

    /**
     * 计算生肖，只计算1900年后出生的人
     *
     * @param year 农历年
     * @return 生肖名
     */
    public static String getChineseZodiac(final int year) {
        if (year < 1900) {
            return null;
        }
        return CHINESE_ZODIACS[(year - 1900) % CHINESE_ZODIACS.length];
    }

}
