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

import org.miaixz.bus.core.center.date.format.FormatPeriod;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.DateKit;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期间隔
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Between implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 开始日期
     */
    private final Date begin;
    /**
     * 结束日期
     */
    private final Date end;

    /**
     * 构造
     * 在前的日期做为起始时间，在后的做为结束时间，间隔只保留绝对值正数
     *
     * @param begin 起始时间
     * @param end   结束时间
     */
    public Between(final Date begin, final Date end) {
        this(begin, end, true);
    }

    /**
     * 构造
     * 在前的日期做为起始时间，在后的做为结束时间
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @param isAbs 日期间隔是否只保留绝对值正数
     */
    public Between(final Date begin, final Date end, final boolean isAbs) {
        Assert.notNull(begin, "Begin date is null !");
        Assert.notNull(end, "End date is null !");

        if (isAbs && begin.after(end)) {
            // 间隔只为正数的情况下，如果开始日期晚于结束日期，置换之
            this.begin = end;
            this.end = begin;
        } else {
            this.begin = begin;
            this.end = end;
        }
    }

    /**
     * 创建
     * 在前的日期做为起始时间，在后的做为结束时间，间隔只保留绝对值正数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @return Between
     */
    public static Between of(final Date begin, final Date end) {
        return new Between(begin, end);
    }

    /**
     * 创建
     * 在前的日期做为起始时间，在后的做为结束时间，间隔只保留绝对值正数
     *
     * @param begin 起始时间
     * @param end   结束时间
     * @param isAbs 日期间隔是否只保留绝对值正数
     * @return Between
     */
    public static Between of(final Date begin, final Date end, final boolean isAbs) {
        return new Between(begin, end, isAbs);
    }

    /**
     * 判断两个日期相差的时长
     * 返回 给定单位的时长差
     *
     * @param unit 相差的单位：相差 天{@link Fields.Units#DAY}、小时{@link Fields.Units#HOUR} 等
     * @return 时长差
     */
    public long between(final Fields.Units unit) {
        final long diff = end.getTime() - begin.getTime();
        return diff / unit.getMillis();
    }

    /**
     * 计算两个日期相差月数
     * 在非重置情况下，如果起始日期的天大于结束日期的天，月数要少算1（不足1个月）
     *
     * @param isReset 是否重置时间为起始时间（重置天时分秒）
     * @return 相差月数
     */
    public long betweenMonth(final boolean isReset) {
        final Calendar beginCal = DateKit.calendar(begin);
        final Calendar endCal = DateKit.calendar(end);

        final int betweenYear = endCal.get(Calendar.YEAR) - beginCal.get(Calendar.YEAR);
        final int betweenMonthOfYear = endCal.get(Calendar.MONTH) - beginCal.get(Calendar.MONTH);

        final int result = betweenYear * 12 + betweenMonthOfYear;
        if (!isReset) {
            endCal.set(Calendar.YEAR, beginCal.get(Calendar.YEAR));
            endCal.set(Calendar.MONTH, beginCal.get(Calendar.MONTH));
            final long between = endCal.getTimeInMillis() - beginCal.getTimeInMillis();
            if (between < 0) {
                return result - 1;
            }
        }
        return result;
    }

    /**
     * 计算两个日期相差年数
     * 在非重置情况下，如果起始日期的月大于结束日期的月，年数要少算1（不足1年）
     *
     * @param isReset 是否重置时间为起始时间（重置月天时分秒）
     * @return 相差年数
     */
    public long betweenYear(final boolean isReset) {
        final Calendar beginCal = DateKit.calendar(begin);
        final Calendar endCal = DateKit.calendar(end);

        final int result = endCal.get(Calendar.YEAR) - beginCal.get(Calendar.YEAR);
        if (false == isReset) {
            final int beginMonthBase0 = beginCal.get(Calendar.MONTH);
            final int endMonthBase0 = endCal.get(Calendar.MONTH);
            if (beginMonthBase0 < endMonthBase0) {
                return result;
            } else if (beginMonthBase0 > endMonthBase0) {
                return result - 1;
            } else if (Calendar.FEBRUARY == beginMonthBase0
                    && Calendars.isLastDayOfMonth(beginCal)
                    && Calendars.isLastDayOfMonth(endCal)) {
                // 考虑闰年的2月情况
                // 两个日期都位于2月的最后一天，此时月数按照相等对待，此时都设置为1号
                beginCal.set(Calendar.DAY_OF_MONTH, 1);
                endCal.set(Calendar.DAY_OF_MONTH, 1);
            }

            endCal.set(Calendar.YEAR, beginCal.get(Calendar.YEAR));
            final long between = endCal.getTimeInMillis() - beginCal.getTimeInMillis();
            if (between < 0) {
                return result - 1;
            }
        }
        return result;
    }

    /**
     * 获取开始时间
     *
     * @return 获取开始时间
     */
    public Date getBegin() {
        return begin;
    }

    /**
     * 获取结束日期
     *
     * @return 结束日期
     */
    public Date getEnd() {
        return end;
    }

    /**
     * 格式化输出时间差
     *
     * @param unit  日期单位
     * @param level 级别
     * @return 字符串
     */
    public String toString(final Fields.Units unit, final FormatPeriod.Level level) {
        return DateKit.formatBetween(between(unit), level);
    }

    /**
     * 格式化输出时间差
     *
     * @param level 级别
     * @return 字符串
     */
    public String toString(final FormatPeriod.Level level) {
        return toString(Fields.Units.MS, level);
    }

    @Override
    public String toString() {
        return toString(FormatPeriod.Level.MILLISECOND);
    }

}
