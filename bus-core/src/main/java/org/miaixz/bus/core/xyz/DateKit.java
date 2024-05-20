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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.center.date.*;
import org.miaixz.bus.core.center.date.format.CustomFormat;
import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.format.FormatPeriod;
import org.miaixz.bus.core.center.date.format.parser.NormalDateParser;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.center.date.format.parser.RegisterDateParser;
import org.miaixz.bus.core.center.date.printer.FormatPrinter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.text.CharsBacker;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 日期时间工具类
 *
 * @author Kimi Liu
 * @see Fields 日期常用格式工具类
 * @since Java 17+
 */
public class DateKit extends Calendars {

    /**
     * 当前时间，转换为{@link DateTime}对象
     *
     * @return 当前时间
     */
    public static DateTime now() {
        return new DateTime();
    }

    /**
     * 当天开始的时间，结果类似：2022-10-26 00:00:00
     *
     * @return 当天开始的时间
     */
    public static DateTime today() {
        return new DateTime(beginOfDay(Calendar.getInstance()));
    }

    /**
     * 当前时间，转换为{@link DateTime}对象，忽略毫秒部分
     *
     * @return 当前时间
     */
    public static DateTime dateSecond() {
        return beginOfSecond(now());
    }

    /**
     * {@link Date}类型时间转为{@link DateTime}
     * 如果date本身为DateTime对象，则返回强转后的对象，否则新建一个DateTime对象
     *
     * @param date {@link Date}，如果传入{@code null}，返回{@code null}
     * @return 时间对象
     */
    public static DateTime date(final Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof DateTime) {
            return (DateTime) date;
        }
        return dateNew(date);
    }

    /**
     * {@link XMLGregorianCalendar}类型时间转为{@link DateTime}
     *
     * @param date {@link XMLGregorianCalendar}，如果传入{@code null}，返回{@code null}
     * @return 时间对象
     */
    public static DateTime date(final XMLGregorianCalendar date) {
        if (date == null) {
            return null;
        }
        return date(date.toGregorianCalendar());
    }

    /**
     * 根据已有{@link Date} 产生新的{@link DateTime}对象
     *
     * @param date Date对象，如果传入{@code null}，返回{@code null}
     * @return {@link DateTime}对象
     */
    public static DateTime dateNew(final Date date) {
        if (date == null) {
            return null;
        }
        return new DateTime(date);
    }

    /**
     * 根据已有{@link Date} 产生新的{@link DateTime}对象，并根据指定时区转换
     *
     * @param date     Date对象，如果传入{@code null}，返回{@code null}
     * @param timeZone 时区，传入{@code null}则使用默认时区
     * @return {@link DateTime}对象
     */
    public static DateTime date(final Date date, final TimeZone timeZone) {
        if (date == null) {
            return null;
        }
        return new DateTime(date, timeZone);
    }

    /**
     * Long类型时间转为{@link DateTime}
     * 只支持毫秒级别时间戳，如果需要秒级别时间戳，请自行×1000L
     *
     * @param date Long类型Date（Unix时间戳）
     * @return 时间对象
     */
    public static DateTime date(final long date) {
        return new DateTime(date);
    }

    /**
     * {@link Calendar}类型时间转为{@link DateTime}
     * 始终根据已有{@link Calendar} 产生新的{@link DateTime}对象
     *
     * @param calendar {@link Calendar}，如果传入{@code null}，返回{@code null}
     * @return 时间对象
     */
    public static DateTime date(final Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new DateTime(calendar);
    }

    /**
     * {@link TemporalAccessor}类型时间转为{@link DateTime}
     * 始终根据已有{@link TemporalAccessor} 产生新的{@link DateTime}对象
     *
     * @param temporalAccessor {@link TemporalAccessor},常用子类： {@link LocalDateTime}、 LocalDate，如果传入{@code null}，返回{@code null}
     * @return 时间对象
     */
    public static DateTime date(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }
        return new DateTime(temporalAccessor);
    }

    /**
     * 当前时间的时间戳
     *
     * @return 时间
     */
    public static long current() {
        return System.currentTimeMillis();
    }

    /**
     * 当前时间的时间戳（秒）
     *
     * @return 当前时间秒数
     */
    public static long currentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 当前时间，格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String formatNow() {
        return formatDateTime(new DateTime());
    }

    /**
     * 当前日期，格式 yyyy-MM-dd
     *
     * @return 当前日期的标准形式字符串
     */
    public static String formatToday() {
        return formatDate(new DateTime());
    }

    /**
     * 获得年的部分
     *
     * @param date 日期
     * @return 年的部分
     */
    public static int year(final Date date) {
        return DateTime.of(date).year();
    }

    /**
     * 获得指定日期所属季度，从1开始计数
     *
     * @param date 日期
     * @return 第几个季度
     */
    public static int quarter(final Date date) {
        return DateTime.of(date).quarter();
    }

    /**
     * 获得指定日期所属季度
     *
     * @param date 日期
     * @return 第几个季度枚举
     */
    public static Fields.Quarter quarterEnum(final Date date) {
        return DateTime.of(date).quarterEnum();
    }

    /**
     * 获得月份，从0开始计数
     *
     * @param date 日期
     * @return 月份，从0开始计数
     */
    public static int month(final Date date) {
        return DateTime.of(date).month();
    }

    /**
     * 获得月份
     *
     * @param date 日期
     * @return {@link Fields.Month}
     */
    public static Fields.Month monthEnum(final Date date) {
        return DateTime.of(date).monthEnum();
    }

    /**
     * 获得指定日期是所在年份的第几周
     * 此方法返回值与一周的第一天有关，比如：
     * 2016年1月3日为周日，如果一周的第一天为周日，那这天是第二周（返回2）
     * 如果一周的第一天为周一，那这天是第一周（返回1）
     * 跨年的那个星期得到的结果总是1
     *
     * @param date 日期
     * @return 周
     * @see DateTime#setFirstDayOfWeek(Fields.Week)
     */
    public static int weekOfYear(final Date date) {
        return DateTime.of(date).weekOfYear();
    }

    /**
     * 获得指定日期是所在月份的第几周
     *
     * @param date 日期
     * @return 周
     */
    public static int weekOfMonth(final Date date) {
        return DateTime.of(date).weekOfMonth();
    }

    /**
     * 获得指定日期是这个日期所在月份的第几天
     *
     * @param date 日期
     * @return 天
     */
    public static int dayOfMonth(final Date date) {
        return DateTime.of(date).dayOfMonth();
    }

    /**
     * 获得指定日期是这个日期所在年的第几天
     *
     * @param date 日期
     * @return 天
     */
    public static int dayOfYear(final Date date) {
        return DateTime.of(date).dayOfYear();
    }

    /**
     * 获得指定日期是星期几，1表示周日，2表示周一
     *
     * @param date 日期
     * @return 天
     */
    public static int dayOfWeek(final Date date) {
        return DateTime.of(date).dayOfWeek();
    }

    /**
     * 获得指定日期是星期几
     *
     * @param date 日期
     * @return {@link Fields.Week}
     */
    public static Fields.Week dayOfWeekEnum(final Date date) {
        return DateTime.of(date).dayOfWeekEnum();
    }

    /**
     * 是否为周末（周六或周日）
     *
     * @param date 判定的日期{@link Date}
     * @return 是否为周末（周六或周日）
     */
    public static boolean isWeekend(final Date date) {
        final Fields.Week week = dayOfWeekEnum(date);
        return Fields.Week.SATURDAY == week || Fields.Week.SUNDAY == week;
    }

    /**
     * 获得指定日期的小时数部分
     *
     * @param date          日期
     * @param is24HourClock 是否24小时制
     * @return 小时数
     */
    public static int hour(final Date date, final boolean is24HourClock) {
        return DateTime.of(date).hour(is24HourClock);
    }

    /**
     * 获得指定日期的分钟数部分
     * 例如：10:04:15.250 =》 4
     *
     * @param date 日期
     * @return 分钟数
     */
    public static int minute(final Date date) {
        return DateTime.of(date).minute();
    }

    /**
     * 获得指定日期的秒数部分
     *
     * @param date 日期
     * @return 秒数
     */
    public static int second(final Date date) {
        return DateTime.of(date).second();
    }

    /**
     * 获得指定日期的毫秒数部分
     *
     * @param date 日期
     * @return 毫秒数
     */
    public static int millisecond(final Date date) {
        return DateTime.of(date).millisecond();
    }

    /**
     * 是否为上午
     *
     * @param date 日期
     * @return 是否为上午
     */
    public static boolean isAM(final Date date) {
        return DateTime.of(date).isAM();
    }

    /**
     * 是否为下午
     *
     * @param date 日期
     * @return 是否为下午
     */
    public static boolean isPM(final Date date) {
        return DateTime.of(date).isPM();
    }

    /**
     * @return 今年
     */
    public static int thisYear() {
        return year(now());
    }

    /**
     * @return 当前月份
     */
    public static int thisMonth() {
        return month(now());
    }

    /**
     * @return 当前月份 {@link Fields.Month}
     */
    public static Fields.Month thisMonthEnum() {
        return monthEnum(now());
    }

    /**
     * @return 当前日期所在年份的第几周
     */
    public static int thisWeekOfYear() {
        return weekOfYear(now());
    }

    /**
     * @return 当前日期所在月份的第几周
     */
    public static int thisWeekOfMonth() {
        return weekOfMonth(now());
    }

    /**
     * @return 当前日期是这个日期所在月份的第几天
     */
    public static int thisDayOfMonth() {
        return dayOfMonth(now());
    }

    /**
     * @return 当前日期是星期几
     */
    public static int thisDayOfWeek() {
        return dayOfWeek(now());
    }

    /**
     * @return 当前日期是星期几 {@link Fields.Week}
     */
    public static Fields.Week thisDayOfWeekEnum() {
        return dayOfWeekEnum(now());
    }

    /**
     * @param is24HourClock 是否24小时制
     * @return 当前日期的小时数部分
     */
    public static int thisHour(final boolean is24HourClock) {
        return hour(now(), is24HourClock);
    }

    /**
     * @return 当前日期的分钟数部分
     */
    public static int thisMinute() {
        return minute(now());
    }

    /**
     * @return 当前日期的秒数部分
     */
    public static int thisSecond() {
        return second(now());
    }

    /**
     * @return 当前日期的毫秒数部分
     */
    public static int thisMillisecond() {
        return millisecond(now());
    }

    /**
     * 获得指定日期年份和季节
     * 格式：[20131]表示2013年第一季度
     *
     * @param date 日期
     * @return Quarter ，类似于 20132
     */
    public static String yearAndQuarter(final Date date) {
        return yearAndQuarter(calendar(date));
    }

    /**
     * 格式化日期时间
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatLocalDateTime(final LocalDateTime localDateTime) {
        return Formatter.formatNormal(localDateTime);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param localDateTime 被格式化的日期
     * @param format        日期格式，常用格式见： {@link Fields}
     * @return 格式化后的字符串
     */
    public static String format(final LocalDateTime localDateTime, final String format) {
        return Formatter.format(localDateTime, format);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format 日期格式，常用格式见： {@link Fields} {@link Fields#NORM_DATETIME}
     * @return 格式化后的字符串
     */
    public static String format(final Date date, final String format) {
        if (null == date || StringKit.isBlank(format)) {
            return null;
        }

        // 检查自定义格式
        if (CustomFormat.isCustomFormat(format)) {
            return CustomFormat.format(date, format);
        }

        TimeZone timeZone = null;
        if (date instanceof DateTime) {
            timeZone = ((DateTime) date).getTimeZone();
        }
        return format(date, newSimpleFormat(format, null, timeZone));
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link FormatPrinter} 或 {@link FormatBuilder} {@link Formatter#NORM_DATETIME_FORMAT}
     * @return 格式化后的字符串
     */
    public static String format(final Date date, final FormatPrinter format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link SimpleDateFormat}
     * @return 格式化后的字符串
     */
    public static String format(final Date date, final DateFormat format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link SimpleDateFormat} {@link Formatter#NORM_DATETIME_FORMATTER}
     * @return 格式化后的字符串
     */
    public static String format(final Date date, final DateTimeFormatter format) {
        if (null == format || null == date) {
            return null;
        }
        // java.time.temporal.UnsupportedTemporalTypeException: Unsupported field: YearOfEra
        // 出现以上报错时，表示Instant时间戳没有时区信息，赋予默认时区
        return Formatter.format(date.toInstant(), format);
    }

    /**
     * 格式化日期时间
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的日期
     */
    public static String formatDateTime(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.NORM_DATETIME_FORMAT.format(date);
    }

    /**
     * 格式化日期部分（不包括时间）
     * 格式 yyyy-MM-dd
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatDate(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.NORM_DATE_FORMAT.format(date);
    }

    /**
     * 格式化时间
     * 格式 HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatTime(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.NORM_TIME_FORMAT.format(date);
    }

    /**
     * 格式化为Http的标准日期格式
     * 标准日期格式遵循RFC 1123规范，格式类似于：Fri, 31 Dec 1999 23:59:59 GMT
     *
     * @param date 被格式化的日期
     * @return HTTP标准形式日期字符串
     */
    public static String formatHttpDate(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.HTTP_DATETIME_FORMAT_GMT.format(date);
    }

    /**
     * 格式化为中文日期格式，如果isUppercase为false，则返回类似：2018年10月24日，否则返回二〇一八年十月二十四日
     *
     * @param date        被格式化的日期
     * @param isUppercase 是否采用大写形式
     * @param withTime    是否包含时间部分
     * @return 中文日期字符串
     */
    public static String formatChineseDate(final Date date, final boolean isUppercase, final boolean withTime) {
        if (null == date) {
            return null;
        }

        if (!isUppercase) {
            return (withTime ? Formatter.CN_DATE_TIME_FORMAT : Formatter.CN_DATE_FORMAT).format(date);
        }

        return Calendars.formatChineseDate(Calendars.calendar(date), withTime);
    }

    /**
     * 构建DateTime对象
     *
     * @param dateStr    Date字符串
     * @param dateFormat 格式化器 {@link SimpleDateFormat}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence dateStr, final DateFormat dateFormat) {
        return new DateTime(dateStr, dateFormat);
    }

    /**
     * 构建DateTime对象
     *
     * @param dateStr Date字符串
     * @param parser  格式化器,{@link FormatBuilder}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence dateStr, final PositionDateParser parser) {
        return new DateTime(dateStr, parser);
    }

    /**
     * 构建DateTime对象
     *
     * @param dateStr Date字符串
     * @param parser  格式化器,{@link FormatBuilder}
     * @param lenient 是否宽容模式
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence dateStr, final PositionDateParser parser, final boolean lenient) {
        return new DateTime(dateStr, parser, lenient);
    }

    /**
     * 构建DateTime对象
     *
     * @param dateStr   Date字符串
     * @param formatter 格式化器,{@link DateTimeFormatter}
     * @return DateTime对象
     */
    public static DateTime parse(final CharSequence dateStr, final DateTimeFormatter formatter) {
        return new DateTime(dateStr, formatter);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param dateStr 特定格式的日期
     * @param format  格式，例如yyyy-MM-dd
     * @return 日期对象
     */
    public static DateTime parse(final CharSequence dateStr, final String format) {
        return new DateTime(dateStr, format);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param dateStr 特定格式的日期
     * @param format  格式，例如yyyy-MM-dd
     * @param locale  区域信息
     * @return 日期对象
     */
    public static DateTime parse(final CharSequence dateStr, final String format, final Locale locale) {
        if (CustomFormat.isCustomFormat(format)) {
            // 自定义格式化器忽略Locale
            return new DateTime(CustomFormat.parse(dateStr, format));
        }
        return new DateTime(dateStr, newSimpleFormat(format, locale, null));
    }

    /**
     * 通过给定的日期格式解析日期时间字符串
     * 传入的日期格式会逐个尝试，直到解析成功，返回{@link DateTime}对象，否则抛出{@link DateException}异常
     *
     * @param text          日期时间字符串，非空
     * @param parsePatterns 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的Date
     * @throws IllegalArgumentException if the date string or pattern array is null
     * @throws DateException            if none of the date patterns were suitable
     */
    public static DateTime parse(final String text, final String... parsePatterns) throws DateException {
        return date(Calendars.parseByPatterns(text, parsePatterns));
    }

    /**
     * 将日期字符串转换为{@link DateTime}对象，格式：
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy/MM/dd HH:mm:ss</li>
     * <li>yyyy.MM.dd HH:mm:ss</li>
     * <li>yyyy年MM月dd日 HH时mm分ss秒</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>HH:mm:ss</li>
     * <li>HH时mm分ss秒</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSSSSS</li>
     * <li>yyyyMMddHHmmss</li>
     * <li>yyyyMMddHHmmssSSS</li>
     * <li>yyyyMMdd</li>
     * <li>EEE, dd MMM yyyy HH:mm:ss z</li>
     * <li>EEE MMM dd HH:mm:ss zzz yyyy</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * </ol>
     *
     * @param dateCharSequence 日期字符串
     * @return 日期
     */
    public static DateTime parse(final CharSequence dateCharSequence) {
        if (StringKit.isBlank(dateCharSequence)) {
            return null;
        }
        String dateStr = dateCharSequence.toString();
        // 去掉两边空格并去掉中文日期中的“日”和“秒”，以规范长度
        dateStr = StringKit.removeAll(dateStr.trim(), '日', '秒');

        final Date result = RegisterDateParser.INSTANCE.parse(dateStr);
        if (null != result) {
            return date(result);
        }

        //标准日期格式（包括单个数字的日期时间）
        dateStr = normalize(dateStr);

        if (NormalDateParser.INSTANCE.test(dateStr)) {
            return NormalDateParser.INSTANCE.parse(dateStr);
        }

        // 没有更多匹配的时间格式
        throw new DateException("No format fit for date String [{}] !", dateStr);
    }

    /**
     * 修改日期为某个时间字段起始时间
     *
     * @param date {@link Date}
     * @param type 保留到的时间字段，如定义为 {@link Fields.Type#SECOND}，表示这个字段不变，这个字段以下字段全部归0
     * @return {@link DateTime}
     */
    public static DateTime truncate(final Date date, final Fields.Type type) {
        return new DateTime(truncate(calendar(date), type));
    }

    /**
     * 修改日期为某个时间字段四舍五入时间
     *
     * @param date {@link Date}
     * @param type 时间字段
     * @return {@link DateTime}
     */
    public static DateTime round(final Date date, final Fields.Type type) {
        return new DateTime(round(calendar(date), type));
    }

    /**
     * 修改日期为某个时间字段结束时间
     *
     * @param date {@link Date}
     * @param type 保留到的时间字段，如定义为 {@link Fields.Type#SECOND}，表示这个字段不变，这个字段以下字段全部取最大值
     * @return {@link DateTime}
     */
    public static DateTime ceiling(final Date date, final Fields.Type type) {
        return new DateTime(ceiling(calendar(date), type));
    }

    /**
     * 修改日期为某个时间字段结束时间
     * 可选是否归零毫秒。
     *
     * <p>
     * 有时候由于毫秒部分必须为0（如MySQL数据库中），因此在此加上选项。
     * </p>
     *
     * @param date                {@link Date}
     * @param type                时间字段
     * @param truncateMillisecond 是否毫秒归零
     * @return {@link DateTime}
     */
    public static DateTime ceiling(final Date date, final Fields.Type type, final boolean truncateMillisecond) {
        return new DateTime(ceiling(calendar(date), type, truncateMillisecond));
    }

    /**
     * 获取秒级别的开始时间，即毫秒部分设置为0
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfSecond(final Date date) {
        return new DateTime(beginOfSecond(calendar(date)));
    }

    /**
     * 获取秒级别的结束时间，即毫秒设置为999
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfSecond(final Date date) {
        return new DateTime(endOfSecond(calendar(date)));
    }

    /**
     * 获取某小时的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfHour(final Date date) {
        return new DateTime(beginOfHour(calendar(date)));
    }

    /**
     * 获取某小时的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfHour(final Date date) {
        return new DateTime(endOfHour(calendar(date)));
    }

    /**
     * 获取某分钟的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfMinute(final Date date) {
        return new DateTime(beginOfMinute(calendar(date)));
    }

    /**
     * 获取某分钟的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfMinute(final Date date) {
        return new DateTime(endOfMinute(calendar(date)));
    }

    /**
     * 获取某天的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfDay(final Date date) {
        return new DateTime(beginOfDay(calendar(date)));
    }

    /**
     * 获取某天的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfDay(final Date date) {
        return new DateTime(endOfDay(calendar(date)));
    }

    /**
     * 获取某周的开始时间，周一定为一周的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfWeek(final Date date) {
        return new DateTime(beginOfWeek(calendar(date)));
    }

    /**
     * 获取某周的开始时间
     *
     * @param date               日期
     * @param isMondayAsFirstDay 是否周一做为一周的第一天（false表示周日做为第一天）
     * @return {@link DateTime}
     */
    public static DateTime beginOfWeek(final Date date, final boolean isMondayAsFirstDay) {
        return new DateTime(beginOfWeek(calendar(date), isMondayAsFirstDay));
    }

    /**
     * 获取某周的结束时间，周日定为一周的结束
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfWeek(final Date date) {
        return new DateTime(endOfWeek(calendar(date)));
    }

    /**
     * 获取某周的结束时间
     *
     * @param date              日期
     * @param isSundayAsLastDay 是否周日做为一周的最后一天（false表示周六做为最后一天）
     * @return {@link DateTime}
     */
    public static DateTime endOfWeek(final Date date, final boolean isSundayAsLastDay) {
        return new DateTime(endOfWeek(calendar(date), isSundayAsLastDay));
    }

    /**
     * 获取某月的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfMonth(final Date date) {
        return new DateTime(beginOfMonth(calendar(date)));
    }

    /**
     * 获取某月的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfMonth(final Date date) {
        return new DateTime(endOfMonth(calendar(date)));
    }

    /**
     * 获取某季度的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfQuarter(final Date date) {
        return new DateTime(beginOfQuarter(calendar(date)));
    }

    /**
     * 获取某季度的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfQuarter(final Date date) {
        return new DateTime(endOfQuarter(calendar(date)));
    }

    /**
     * 获取某年的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfYear(final Date date) {
        return new DateTime(beginOfYear(calendar(date)));
    }

    /**
     * 获取某年的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfYear(final Date date) {
        return new DateTime(endOfYear(calendar(date)));
    }

    /**
     * 昨天
     *
     * @return 昨天
     */
    public static DateTime yesterday() {
        return offsetDay(new DateTime(), -1);
    }

    /**
     * 明天
     *
     * @return 明天
     */
    public static DateTime tomorrow() {
        return offsetDay(new DateTime(), 1);
    }

    /**
     * 上周
     *
     * @return 上周
     */
    public static DateTime lastWeek() {
        return offsetWeek(new DateTime(), -1);
    }

    /**
     * 下周
     *
     * @return 下周
     */
    public static DateTime nextWeek() {
        return offsetWeek(new DateTime(), 1);
    }

    /**
     * 上个月
     *
     * @return 上个月
     */
    public static DateTime lastMonth() {
        return offsetMonth(new DateTime(), -1);
    }

    /**
     * 下个月
     *
     * @return 下个月
     */
    public static DateTime nextMonth() {
        return offsetMonth(new DateTime(), 1);
    }

    /**
     * 偏移毫秒数
     *
     * @param date   日期
     * @param offset 偏移毫秒数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMillisecond(final Date date, final int offset) {
        return offset(date, Fields.Type.MILLISECOND, offset);
    }

    /**
     * 偏移秒数
     *
     * @param date   日期
     * @param offset 偏移秒数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetSecond(final Date date, final int offset) {
        return offset(date, Fields.Type.SECOND, offset);
    }

    /**
     * 偏移分钟
     *
     * @param date   日期
     * @param offset 偏移分钟数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMinute(final Date date, final int offset) {
        return offset(date, Fields.Type.MINUTE, offset);
    }

    /**
     * 偏移小时
     *
     * @param date   日期
     * @param offset 偏移小时数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetHour(final Date date, final int offset) {
        return offset(date, Fields.Type.HOUR_OF_DAY, offset);
    }

    /**
     * 偏移天
     *
     * @param date   日期
     * @param offset 偏移天数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetDay(final Date date, final int offset) {
        return offset(date, Fields.Type.DAY_OF_YEAR, offset);
    }

    /**
     * 偏移周
     *
     * @param date   日期
     * @param offset 偏移周数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetWeek(final Date date, final int offset) {
        return offset(date, Fields.Type.WEEK_OF_YEAR, offset);
    }

    /**
     * 偏移月
     *
     * @param date   日期
     * @param offset 偏移月数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMonth(final Date date, final int offset) {
        return offset(date, Fields.Type.MONTH, offset);
    }

    /**
     * 获取指定日期偏移指定时间后的时间，生成的偏移日期不影响原日期
     *
     * @param date   基准日期
     * @param type   偏移的粒度大小（小时、天、月等）{@link Fields.Type}
     * @param offset 偏移量，正数为向后偏移，负数为向前偏移
     * @return 偏移后的日期
     */
    public static DateTime offset(final Date date, final Fields.Type type, final int offset) {
        return dateNew(date).offset(type, offset);
    }

    /**
     * 判断两个日期相差的时长，只保留绝对值
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param unit      相差的单位：相差 天{@link Fields.Units#DAY}、小时{@link Fields.Units#HOUR} 等
     * @return 日期差
     */
    public static long between(final Date beginDate, final Date endDate, final Fields.Units unit) {
        return between(beginDate, endDate, unit, true);
    }

    /**
     * 判断两个日期相差的时长
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param unit      相差的单位：相差 天{@link Fields.Units#DAY}、小时{@link Fields.Units#HOUR} 等
     * @param isAbs     日期间隔是否只保留绝对值正数
     * @return 日期差
     */
    public static long between(final Date beginDate, final Date endDate, final Fields.Units unit, final boolean isAbs) {
        return new Between(beginDate, endDate, isAbs).between(unit);
    }

    /**
     * 判断两个日期相差的毫秒数
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 日期差
     */
    public static long betweenMs(final Date beginDate, final Date endDate) {
        return new Between(beginDate, endDate).between(Fields.Units.MS);
    }

    /**
     * 判断两个日期相差的天数
     *
     * <pre>
     * 有时候我们计算相差天数的时候需要忽略时分秒。
     * 比如：2016-02-01 23:59:59和2016-02-02 00:00:00相差一秒
     * 如果isReset为{@code false}相差天数为0。
     * 如果isReset为{@code true}相差天数将被计算为1
     * </pre>
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间
     * @return 日期差
     */
    public static long betweenDay(Date beginDate, Date endDate, final boolean isReset) {
        if (isReset) {
            beginDate = beginOfDay(beginDate);
            endDate = beginOfDay(endDate);
        }
        return between(beginDate, endDate, Fields.Units.DAY);
    }

    /**
     * 计算指定时间区间内的周数
     *
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @param isReset   是否重置时间为起始时间
     * @return 周数
     */
    public static long betweenWeek(Date beginDate, Date endDate, final boolean isReset) {
        if (isReset) {
            beginDate = beginOfDay(beginDate);
            endDate = beginOfDay(endDate);
        }
        return between(beginDate, endDate, Fields.Units.WEEK);
    }

    /**
     * 计算两个日期相差月数
     * 在非重置情况下，如果起始日期的天大于结束日期的天，月数要少算1（不足1个月）
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间（重置天时分秒）
     * @return 相差月数
     */
    public static long betweenMonth(final Date beginDate, final Date endDate, final boolean isReset) {
        return new Between(beginDate, endDate).betweenMonth(isReset);
    }

    /**
     * 计算两个日期相差年数
     * 在非重置情况下，如果起始日期的月大于结束日期的月，年数要少算1（不足1年）
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间（重置月天时分秒）
     * @return 相差年数
     */
    public static long betweenYear(final Date beginDate, final Date endDate, final boolean isReset) {
        return new Between(beginDate, endDate).betweenYear(isReset);
    }

    /**
     * 格式化日期间隔输出
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param level     级别，按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒
     */
    public static String formatBetween(final Date beginDate, final Date endDate, final FormatPeriod.Level level) {
        return formatBetween(between(beginDate, endDate, Fields.Units.MS), level);
    }

    /**
     * 格式化日期间隔输出，精确到毫秒
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return XX天XX小时XX分XX秒
     */
    public static String formatBetween(final Date beginDate, final Date endDate) {
        return formatBetween(between(beginDate, endDate, Fields.Units.MS));
    }

    /**
     * 格式化日期间隔输出
     *
     * @param betweenMs 日期间隔
     * @param level     级别，按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒XX毫秒
     */
    public static String formatBetween(final long betweenMs, final FormatPeriod.Level level) {
        return FormatPeriod.of(betweenMs, level).format();
    }

    /**
     * 格式化日期间隔输出，精确到毫秒
     *
     * @param betweenMs 日期间隔
     * @return XX天XX小时XX分XX秒XX毫秒
     */
    public static String formatBetween(final long betweenMs) {
        return FormatPeriod.of(betweenMs, FormatPeriod.Level.MILLISECOND).format();
    }

    /**
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param date      被检查的日期
     * @param beginDate 起始日期（包含）
     * @param endDate   结束日期（包含）
     * @return 是否在范围内
     */
    public static boolean isIn(final Date date, final Date beginDate, final Date endDate) {
        return isIn(date, beginDate, endDate, true, true);
    }

    /**
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     * 通过includeBegin, includeEnd参数控制日期范围区间是否为开区间，例如：传入参数：includeBegin=true, includeEnd=false，
     * 则本方法会判断 date ∈ (beginDate, endDate] 是否成立
     *
     * @param date         被检查的日期
     * @param beginDate    起始日期
     * @param endDate      结束日期
     * @param includeBegin 时间范围是否包含起始日期
     * @param includeEnd   时间范围是否包含结束日期
     * @return 是否在范围内
     */
    public static boolean isIn(final Date date, final Date beginDate, final Date endDate,
                               final boolean includeBegin, final boolean includeEnd) {
        return new DateTime().isIn(date, beginDate, endDate, includeBegin, includeEnd);
    }

    /**
     * 是否为相同时间
     * 此方法比较两个日期的时间戳是否相同
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为相同时间
     */
    public static boolean isSameTime(final Date date1, final Date date2) {
        return date1.compareTo(date2) == 0;
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return Calendars.isSameDay(calendar(date1), calendar(date2));
    }

    /**
     * 比较两个日期是否为同一周
     *
     * @param date1 日期1
     * @param date2 日期2
     * @param isMon 是否为周一。国内第一天为星期一，国外第一天为星期日
     * @return 是否为同一周
     */
    public static boolean isSameWeek(final Date date1, final Date date2, final boolean isMon) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return Calendars.isSameWeek(calendar(date1), calendar(date2), isMon);
    }

    /**
     * 比较两个日期是否为同一月
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一月
     */
    public static boolean isSameMonth(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return Calendars.isSameMonth(calendar(date1), calendar(date2));
    }

    /**
     * 计时，常用于记录某段代码的执行时间，单位：纳秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差，纳秒
     */
    public static long spendNt(final long preTime) {
        return System.nanoTime() - preTime;
    }

    /**
     * 计时，常用于记录某段代码的执行时间，单位：毫秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差，毫秒
     */
    public static long spendMs(final long preTime) {
        return System.currentTimeMillis() - preTime;
    }

    /**
     * 创建秒表{@link StopWatch}，用于对代码块的执行时间计数
     * <p>
     * 使用方法如下：
     *
     * <pre>
     * StopWatch stopWatch = DateKit.createStopWatch();
     *
     * // 任务1
     * stopWatch.start("任务一");
     * Thread.sleep(1000);
     * stopWatch.stop();
     *
     * // 任务2
     * stopWatch.start("任务二");
     * Thread.sleep(2000);
     * stopWatch.stop();
     *
     * // 打印出耗时
     * Console.log(stopWatch.prettyPrint());
     *
     * </pre>
     *
     * @return {@link StopWatch}
     */
    public static StopWatch createStopWatch() {
        return new StopWatch();
    }

    /**
     * 创建秒表{@link StopWatch}，用于对代码块的执行时间计数
     * <p>
     * 使用方法如下：
     *
     * <pre>
     * StopWatch stopWatch = DateKit.createStopWatch("任务名称");
     *
     * // 任务1
     * stopWatch.start("任务一");
     * Thread.sleep(1000);
     * stopWatch.stop();
     *
     * // 任务2
     * stopWatch.start("任务二");
     * Thread.sleep(2000);
     * stopWatch.stop();
     *
     * // 打印出耗时
     * Console.log(stopWatch.prettyPrint());
     *
     * </pre>
     *
     * @param id 用于标识秒表的唯一ID
     * @return {@link StopWatch}
     */
    public static StopWatch createStopWatch(final String id) {
        return new StopWatch(id);
    }

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日，标准日期字符串
     * @return 年龄
     */
    public static int ageOfNow(final String birthDay) {
        return ageOfNow(parse(birthDay));
    }

    /**
     * 生日转为年龄，计算法定年龄（周岁）
     * 按照《最高人民法院关于审理未成年人刑事案件具体应用法律若干问题的解释》第二条规定刑法第十七条规定的“周岁”，按照公历的年、月、日计算，从周岁生日的第二天起算。
     * <ul>
     *     <li>2022-03-01出生，则相对2023-03-01，周岁为0，相对于2023-03-02才是1岁。</li>
     *     <li>1999-02-28出生，则相对2000-02-29，周岁为1</li>
     * </ul>
     *
     * @param birthDay 生日
     * @return 年龄
     */
    public static int ageOfNow(final Date birthDay) {
        return age(birthDay, now());
    }

    /**
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(final int year) {
        return Year.isLeap(year);
    }

    /**
     * 计算相对于dateToCompare的年龄，常用于计算指定生日在某年的年龄
     * 按照《最高人民法院关于审理未成年人刑事案件具体应用法律若干问题的解释》第二条规定刑法第十七条规定的“周岁”，按照公历的年、月、日计算，从周岁生日的第二天起算。
     * <ul>
     *     <li>2022-03-01出生，则相对2023-03-01，周岁为0，相对于2023-03-02才是1岁。</li>
     *     <li>1999-02-28出生，则相对2000-02-29，周岁为1</li>
     * </ul>
     *
     * @param birthday      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int age(final Date birthday, Date dateToCompare) {
        if (null == dateToCompare) {
            dateToCompare = now();
        }
        return age(birthday.getTime(), dateToCompare.getTime());
    }

    /**
     * HH:mm:ss 时间格式字符串转为秒数
     * 参考：<a href="https://github.com/iceroot">https://github.com/iceroot</a>
     *
     * @param timeStr 字符串时分秒(HH:mm:ss)格式
     * @return 时分秒转换后的秒数
     */
    public static int timeToSecond(final String timeStr) {
        if (StringKit.isEmpty(timeStr)) {
            return 0;
        }

        final List<String> hms = CharsBacker.split(timeStr, Symbol.COLON, 3, true, true);
        final int lastIndex = hms.size() - 1;

        int result = 0;
        for (int i = lastIndex; i >= 0; i--) {
            result += (int) (Integer.parseInt(hms.get(i)) * Math.pow(60, (lastIndex - i)));
        }
        return result;
    }

    /**
     * 秒数转为时间格式(HH:mm:ss)
     * 参考：<a href="https://github.com/iceroot">https://github.com/iceroot</a>
     *
     * @param seconds 需要转换的秒数
     * @return 转换后的字符串
     */
    public static String secondToTime(final int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must be a positive number!");
        }

        final int hour = seconds / 3600;
        final int other = seconds % 3600;
        final int minute = other / 60;
        final int second = other % 60;
        final StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        sb.append(Symbol.COLON);
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
        sb.append(Symbol.COLON);
        if (second < 10) {
            sb.append("0");
        }
        sb.append(second);
        return sb.toString();
    }

    /**
     * 创建日期范围生成器
     *
     * @param start 起始日期时间（包括）
     * @param end   结束日期时间
     * @param unit  步进单位
     * @return {@link Boundary}
     */
    public static Boundary range(final Date start, final Date end, final Fields.Type unit) {
        return new Boundary(start, end, unit);
    }

    /**
     * 俩个时间区间取交集
     *
     * @param start 开始区间
     * @param end   结束区间
     * @return true 包含
     */
    public static List<DateTime> rangeContains(final Boundary start, final Boundary end) {
        final List<DateTime> startDateTimes = ListKit.of((Iterable<DateTime>) start);
        final List<DateTime> endDateTimes = ListKit.of((Iterable<DateTime>) end);
        return startDateTimes.stream().filter(endDateTimes::contains).collect(Collectors.toList());
    }

    /**
     * 俩个时间区间取差集(end - start)
     *
     * @param start 开始区间
     * @param end   结束区间
     * @return true 包含
     */
    public static List<DateTime> rangeNotContains(final Boundary start, final Boundary end) {
        final List<DateTime> startDateTimes = ListKit.of((Iterable<DateTime>) start);
        final List<DateTime> endDateTimes = ListKit.of((Iterable<DateTime>) end);
        return endDateTimes.stream().filter(item -> !startDateTimes.contains(item)).collect(Collectors.toList());
    }

    /**
     * 按日期范围遍历，执行 function
     *
     * @param start 起始日期时间（包括）
     * @param end   结束日期时间
     * @param unit  步进单位
     * @param func  每次遍历要执行的 function
     * @param <T>   Date经过函数处理结果类型
     * @return 结果列表
     */
    public static <T> List<T> rangeFunc(final Date start, final Date end, final Fields.Type unit, final Function<Date, T> func) {
        if (start == null || end == null || start.after(end)) {
            return Collections.emptyList();
        }
        final ArrayList<T> list = new ArrayList<>();
        for (final DateTime date : range(start, end, unit)) {
            list.add(func.apply(date));
        }
        return list;
    }

    /**
     * 按日期范围遍历，执行 consumer
     *
     * @param start    起始日期时间（包括）
     * @param end      结束日期时间
     * @param unit     步进单位
     * @param consumer 每次遍历要执行的 consumer
     */
    public static void rangeConsume(final Date start, final Date end, final Fields.Type unit, final Consumer<Date> consumer) {
        if (start == null || end == null || start.after(end)) {
            return;
        }
        range(start, end, unit).forEach(consumer);
    }

    /**
     * 根据步进单位获取起始日期时间和结束日期时间的时间区间集合
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @param unit  步进单位
     * @return {@link Boundary}
     */
    public static List<DateTime> rangeToList(final Date start, final Date end, final Fields.Type unit) {
        return ListKit.of((Iterable<DateTime>) range(start, end, unit));
    }

    /**
     * 根据步进单位和步进获取起始日期时间和结束日期时间的时间区间集合
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @param unit  步进单位
     * @param step  步进
     * @return {@link Boundary}
     */
    public static List<DateTime> rangeToList(final Date start, final Date end, final Fields.Type unit, final int step) {
        return ListKit.of((Iterable<DateTime>) new Boundary(start, end, unit, step));
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数
     * @param day   天
     * @return 星座名
     */
    public static String getZodiac(final int month, final int day) {
        return Zodiac.getZodiac(month, day);
    }

    /**
     * 计算生肖，只计算1900年后出生的人
     *
     * @param year 农历年
     * @return 生肖名
     */
    public static String getChineseZodiac(final int year) {
        return Zodiac.getChineseZodiac(year);
    }

    /**
     * {@code null}安全的日期比较，{@code null}对象排在末尾
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 比较结果，如果date1 &lt; date2，返回数小于0，date1==date2返回0，date1 &gt; date2 大于0
     */
    public static int compare(final Date date1, final Date date2) {
        return CompareKit.compare(date1, date2);
    }

    /**
     * {@code null}安全的日期比较，并只比较指定格式； {@code null}对象排在末尾, 并指定日期格式；
     *
     * @param date1  日期1
     * @param date2  日期2
     * @param format 日期格式，常用格式见： {@link Fields}; 允许为空； date1 date2; eg: yyyy-MM-dd
     * @return 比较结果，如果date1 &lt; date2，返回数小于0，date1==date2返回0，date1 &gt; date2 大于0
     */
    public static int compare(Date date1, Date date2, final String format) {
        if (format != null) {
            if (date1 != null) {
                date1 = parse(format(date1, format), format);
            }
            if (date2 != null) {
                date2 = parse(format(date2, format), format);
            }
        }
        return CompareKit.compare(date1, date2);
    }

    /**
     * 纳秒转毫秒
     *
     * @param duration 时长
     * @return 时长毫秒
     */
    public static long nanosToMillis(final long duration) {
        return TimeUnit.NANOSECONDS.toMillis(duration);
    }

    /**
     * 纳秒转秒，保留小数
     *
     * @param duration 时长
     * @return 秒
     */
    public static double nanosToSeconds(final long duration) {
        return duration / 1_000_000_000.0;
    }

    /**
     * Date对象转换为{@link Instant}对象
     *
     * @param date Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(final Date date) {
        return null == date ? null : date.toInstant();
    }

    /**
     * Date对象转换为{@link Instant}对象
     *
     * @param temporalAccessor Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(final TemporalAccessor temporalAccessor) {
        return Almanac.toInstant(temporalAccessor);
    }

    /**
     * {@link Instant} 转换为 {@link LocalDateTime}，使用系统默认时区
     *
     * @param instant {@link Instant}
     * @return {@link LocalDateTime}
     * @see Resolver#of(TemporalAccessor)
     */
    public static LocalDateTime toLocalDateTime(final Instant instant) {
        return Resolver.of(instant);
    }

    /**
     * {@link Date} 转换为 {@link LocalDateTime}，使用系统默认时区
     *
     * @param date {@link Date}
     * @return {@link LocalDateTime}
     * @see Resolver#of(Date)
     */
    public static LocalDateTime toLocalDateTime(final Date date) {
        return Resolver.of(date);
    }

    /**
     * 获得指定年份的总天数
     *
     * @param year 年份
     * @return 天
     */
    public static int lengthOfYear(final int year) {
        return Year.of(year).length();
    }

    /**
     * 获得指定月份的总天数
     *
     * @param month      月份
     * @param isLeapYear 是否闰年
     * @return 天
     */
    public static int lengthOfMonth(final int month, final boolean isLeapYear) {
        return java.time.Month.of(month).length(isLeapYear);
    }

    /**
     * 创建{@link SimpleDateFormat}，注意此对象非线程安全！
     * 此对象默认为严格格式模式，即parse时如果格式不正确会报错。
     *
     * @param pattern 表达式
     * @return {@link SimpleDateFormat}
     */
    public static SimpleDateFormat newSimpleFormat(final String pattern) {
        return newSimpleFormat(pattern, null, null);
    }

    /**
     * 创建{@link SimpleDateFormat}，注意此对象非线程安全！
     * 此对象默认为严格格式模式，即parse时如果格式不正确会报错。
     *
     * @param pattern  表达式
     * @param locale   {@link Locale}，{@code null}表示默认
     * @param timeZone {@link TimeZone}，{@code null}表示默认
     * @return {@link SimpleDateFormat}
     */
    public static SimpleDateFormat newSimpleFormat(final String pattern, Locale locale, final TimeZone timeZone) {
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        final SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
        if (null != timeZone) {
            format.setTimeZone(timeZone);
        }
        format.setLenient(false);
        return format;
    }

    /**
     * 获取时长单位简写
     *
     * @param unit 单位
     * @return 单位简写名称
     */
    public static String getShortName(final TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "μs";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            default:
                return unit.name().toLowerCase();
        }
    }

    /**
     * 检查两个时间段是否有时间重叠
     * 重叠指两个时间段是否有交集，注意此方法时间段重合时如：
     * <ul>
     *     <li>此方法未纠正开始时间小于结束时间</li>
     *     <li>当realStartTime和realEndTime或startTime和endTime相等时,退化为判断区间是否包含点</li>
     *     <li>当realStartTime和realEndTime和startTime和endTime相等时,退化为判断点与点是否相等</li>
     * </ul>
     * See <a href="https://www.ics.uci.edu/~alspaugh/cls/shr/allen.html">准确的区间关系参考:艾伦区间代数</a>
     *
     * @param realStartTime 第一个时间段的开始时间
     * @param realEndTime   第一个时间段的结束时间
     * @param startTime     第二个时间段的开始时间
     * @param endTime       第二个时间段的结束时间
     * @return true 表示时间有重合或包含或相等
     */
    public static boolean isOverlap(final Date realStartTime, final Date realEndTime,
                                    final Date startTime, final Date endTime) {
        // x>b||a>y 无交集
        // 则有交集的逻辑为 !(x>b||a>y)
        // 根据德摩根公式，可化简为 x<=b && a<=y 即 realStartTime<=endTime && startTime<=realEndTime
        return realStartTime.compareTo(endTime) <= 0 && startTime.compareTo(realEndTime) <= 0;
    }

    /**
     * 是否为本月最后一天
     *
     * @param date {@link Date}
     * @return 是否为本月最后一天
     */
    public static boolean isLastDayOfMonth(final Date date) {
        return date(date).isLastDayOfMonth();
    }

    /**
     * 获得本月的最后一天
     *
     * @param date {@link Date}
     * @return 天
     */
    public static int getLastDayOfMonth(final Date date) {
        return date(date).getLastDayOfMonth();
    }

    /**
     * 标准化日期，默认处理以空格区分的日期时间格式，空格前为日期，空格后为时间：
     * 将以下字符替换为"-"
     *
     * <pre>
     * "."
     * "/"
     * "年"
     * "月"
     * </pre>
     * <p>
     * 将以下字符去除
     *
     * <pre>
     * "日"
     * </pre>
     * <p>
     * 将以下字符替换为":"
     *
     * <pre>
     * "时"
     * "分"
     * "秒"
     * </pre>
     * <p>
     * 当末位是":"时去除之（不存在毫秒时）
     *
     * @param dateStr 日期时间字符串
     * @return 格式化后的日期字符串
     */
    private static String normalize(final CharSequence dateStr) {
        if (StringKit.isBlank(dateStr)) {
            return StringKit.toString(dateStr);
        }

        // 日期时间分开处理
        final List<String> dateAndTime = CharsBacker.splitTrim(dateStr, Symbol.SPACE);
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            // 非可被标准处理的格式
            return StringKit.toString(dateStr);
        }

        final StringBuilder builder = StringKit.builder();

        // 日期部分（"\"、"/"、"."、"年"、"月"都替换为"-"）
        String datePart = dateAndTime.get(0).replaceAll("[/.年月]", Symbol.MINUS);
        datePart = StringKit.removeSuffix(datePart, "日");
        builder.append(datePart);

        // 时间部分
        if (size == 2) {
            builder.append(Symbol.C_SPACE);
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", Symbol.COLON);
            timePart = StringKit.removeSuffix(timePart, Symbol.COLON);
            //将ISO8601中的逗号替换为.
            timePart = timePart.replace(',', '.');
            builder.append(timePart);
        }

        return builder.toString();
    }

    /**
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param beginDate 起始日期（包含）
     * @param endDate   结束日期（包含）
     * @return 是否在范围内
     */
    public boolean isIn(final Date beginDate, final Date endDate) {
        return new DateTime().isIn(beginDate, endDate);
    }

    /**
     * {@code java.sql.*}日期时间相关封装
     * 考虑到JDK9+模块化后，java.sql并非默认引入模块，因此将相关内容单独封装为工具，避免类找不到问题。
     */
    public static class SQL {

        /**
         * 转为{@link Timestamp}
         *
         * @param date 日期时间，非空
         * @return {@link Timestamp}
         */
        public static Timestamp timestamp(final Date date) {
            Assert.notNull(date);
            return new Timestamp(date.getTime());
        }

        /**
         * /**
         * 转为{@link java.sql.Date}
         *
         * @param date 日期时间，非空
         * @return {@link java.sql.Date}
         */
        public static java.sql.Date date(final Date date) {
            Assert.notNull(date);
            return new java.sql.Date(date.getTime());
        }

        /**
         * /**
         * 转为{@link java.sql.Time}
         *
         * @param date 日期时间，非空
         * @return {@link java.sql.Time}
         */
        public static java.sql.Time time(final Date date) {
            Assert.notNull(date);
            return new java.sql.Time(date.getTime());
        }

        /**
         * 时间戳转为子类型，支持：
         * <ul>
         *     <li>{@link Date}</li>
         *     <li>{@link DateTime}</li>
         *     <li>{@link java.sql.Date}</li>
         *     <li>{@link java.sql.Time}</li>
         *     <li>{@link Timestamp}</li>
         * </ul>
         *
         * @param <T>         日期类型
         * @param targetClass 目标类型
         * @param mills       Date
         * @return 目标类型对象
         */
        public static <T extends Date> T wrap(final Class<?> targetClass, final long mills) {
            // 返回指定类型
            if (Date.class == targetClass) {
                return (T) new Date(mills);
            }
            if (DateTime.class == targetClass) {
                return (T) DateKit.date(mills);
            }
            if (java.sql.Date.class == targetClass) {
                return (T) new java.sql.Date(mills);
            }
            if (java.sql.Time.class == targetClass) {
                return (T) new java.sql.Time(mills);
            }
            if (Timestamp.class == targetClass) {
                return (T) new Timestamp(mills);
            }

            throw new UnsupportedOperationException(StringKit.format("Unsupported target Date type: {}", targetClass.getName()));
        }
    }

}
