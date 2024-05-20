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
package org.miaixz.bus.core.center.date.chinese;

import java.time.LocalDate;

/**
 * 天干地支类
 * 天干地支，简称为干支
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GanZhi {

    /**
     * 十天干：甲（jiǎ）、乙（yǐ）、丙（bǐng）、丁（dīng）、戊（wù）、己（jǐ）、庚（gēng）、辛（xīn）、壬（rén）、癸（guǐ）
     * 十二地支：子（zǐ）、丑（chǒu）、寅（yín）、卯（mǎo）、辰（chén）、巳（sì）、午（wǔ）、未（wèi）、申（shēn）、酉（yǒu）、戌（xū）、亥（hài）
     * 十二地支对应十二生肖:子-鼠，丑-牛，寅-虎，卯-兔，辰-龙，巳-蛇， 午-马，未-羊，申-猴，酉-鸡，戌-狗，亥-猪
     *
     * @see <a href="https://baike.baidu.com/item/%E5%A4%A9%E5%B9%B2%E5%9C%B0%E6%94%AF/278140">天干地支：简称，干支</a>
     */
    private static final String[] GAN = new String[]{"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    private static final String[] ZHI = new String[]{"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};

    /**
     * 传入 月日的offset 传回干支, 0=甲子
     *
     * @param num 月日的offset
     * @return 干支
     */
    public static String cyclicalm(final int num) {
        return (GAN[num % 10] + ZHI[num % 12]);
    }

    /**
     * 传入年传回干支
     *
     * @param year 农历年
     * @return 干支
     */
    public static String getGanzhiOfYear(final int year) {
        // 1864年（1900 - 36）是甲子年，用于计算基准的干支年
        return cyclicalm(year - org.miaixz.bus.core.center.date.chinese.LunarInfo.BASE_YEAR + 36);
    }

    /**
     * 获取干支月
     *
     * @param year  公历年
     * @param month 公历月，从1开始
     * @param day   公历日
     * @return 干支月
     */
    public static String getGanzhiOfMonth(final int year, final int month, final int day) {
        // 返回当月「节」为几日开始
        final int firstNode = org.miaixz.bus.core.center.date.chinese.SolarTerms.getTerm(year, (month * 2 - 1));
        // 依据12节气修正干支月
        int monthOffset = (year - org.miaixz.bus.core.center.date.chinese.LunarInfo.BASE_YEAR) * 12 + month + 11;
        if (day >= firstNode) {
            monthOffset++;
        }
        return cyclicalm(monthOffset);
    }

    /**
     * 获取干支日
     *
     * @param year       公历年
     * @param monthBase1 公历月，从1开始
     * @param day        公历日
     * @return 干支
     */
    public static String getGanzhiOfDay(final int year, final int monthBase1, final int day) {
        // 与1970-01-01相差天数，不包括当天
        final long days = LocalDate.of(year, monthBase1, day).toEpochDay() - 1;
        // 1899-12-21是农历1899年腊月甲子日  41：相差1900-01-31有41天
        return cyclicalm((int) (days - org.miaixz.bus.core.center.date.chinese.LunarInfo.BASE_DAY + 41));
    }

}
