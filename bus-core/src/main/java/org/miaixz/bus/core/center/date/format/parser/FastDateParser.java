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
package org.miaixz.bus.core.center.date.format.parser;

import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.printer.FastDatePrinter;
import org.miaixz.bus.core.center.date.printer.SimpleDatePrinter;
import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link java.text.SimpleDateFormat} 的线程安全版本，用于解析日期字符串并转换为 {@link Date} 对象
 *
 * @author Kimi Liu
 * @see FastDatePrinter
 * @since Java 17+
 */
public class FastDateParser extends SimpleDatePrinter implements PositionDateParser {

    static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");

    private static final long serialVersionUID = -1L;
    // comparator used to sort regex alternatives
    // alternatives should be ordered longer first, and shorter last. ('february' before 'feb')
    // all entries must be lowercase by locale.
    private static final Comparator<String> LONGER_FIRST_LOWERCASE = Comparator.reverseOrder();
    // OK because we are creating an array with no entries
    private static final ConcurrentMap<Locale, Strategy>[] CACHES = new ConcurrentMap[Calendar.FIELD_COUNT];
    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue < 100 ? parser.adjustYear(iValue) : iValue;
        }
    };
    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(Calendar.MONTH) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue - 1;
        }
    };
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR);
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_YEAR);
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_MONTH);

    // helper classes to parse the format string
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.DAY_OF_YEAR);
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_MONTH);
    private static final Strategy DAY_OF_WEEK_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue != 7 ? iValue + 1 : Calendar.SUNDAY;
        }
    };

    // Serializing
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK_IN_MONTH);
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY);
    private static final Strategy HOUR24_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 24 ? 0 : iValue;
        }
    };
    private static final Strategy HOUR12_STRATEGY = new NumberStrategy(Calendar.HOUR) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 12 ? 0 : iValue;
        }
    };

    // Support for strategies
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR);
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(Calendar.MINUTE);
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(Calendar.SECOND);
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(Calendar.MILLISECOND);
    /**
     * 世纪：2000年前为19， 之后为20
     */
    private final int century;
    private final int startYear;
    // derived fields
    private transient List<StrategyAndWidth> patterns;

    /**
     * <p>
     * Constructs a new FastDateParser.
     * </p>
     * <p>
     * Use {@link FormatBuilder#getInstance(String, TimeZone, Locale)} or another variation of the factory methods of {@link FormatBuilder} to get a cached FastDateParser instance.
     *
     * @param pattern  non-null {@link java.text.SimpleDateFormat} compatible pattern
     * @param timeZone non-null time zone to use
     * @param locale   non-null locale
     */
    public FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * <p>
     * Constructs a new FastDateParser.
     * </p>
     *
     * @param pattern      non-null {@link java.text.SimpleDateFormat} compatible pattern
     * @param timeZone     non-null time zone to use
     * @param locale       non-null locale
     * @param centuryStart The start of the century for 2 digit year parsing
     */
    public FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        super(pattern, timeZone, locale);
        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);

        final int centuryStartYear;
        if (centuryStart != null) {
            definingCalendar.setTime(centuryStart);
            centuryStartYear = definingCalendar.get(Calendar.YEAR);
        } else if (locale.equals(JAPANESE_IMPERIAL)) {
            centuryStartYear = 0;
        } else {
            // from 80 years ago to 20 years from now
            definingCalendar.setTime(new Date());
            centuryStartYear = definingCalendar.get(Calendar.YEAR) - 80;
        }
        century = centuryStartYear / 100 * 100;
        startYear = centuryStartYear - century;

        init(definingCalendar);
    }

    private static boolean isFormatLetter(final char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    private static StringBuilder simpleQuote(final StringBuilder sb, final String value) {
        for (int i = 0; i < value.length(); ++i) {
            final char c = value.charAt(i);
            switch (c) {
                case '\\':
                case '^':
                case Symbol.C_DOLLAR:
                case '.':
                case '|':
                case '?':
                case Symbol.C_STAR:
                case Symbol.C_PLUS:
                case Symbol.C_PARENTHESE_LEFT:
                case ')':
                case '[':
                case '{':
                    sb.append('\\');
                default:
                    sb.append(c);
            }
        }
        return sb;
    }

    /**
     * Get the short and long values displayed for a field
     *
     * @param cal    The calendar to obtain the short and long values
     * @param locale The locale of display names
     * @param field  The field of interest
     * @param regex  The regular expression to build
     * @return The map of string display names to field values
     */
    private static Map<String, Integer> appendDisplayNames(final Calendar cal, final Locale locale, final int field, final StringBuilder regex) {
        final Map<String, Integer> values = new HashMap<>();

        final Map<String, Integer> displayNames = cal.getDisplayNames(field, Calendar.ALL_STYLES, locale);
        final TreeSet<String> sorted = new TreeSet<>(LONGER_FIRST_LOWERCASE);
        for (final Map.Entry<String, Integer> displayName : displayNames.entrySet()) {
            final String key = displayName.getKey().toLowerCase(locale);
            if (sorted.add(key)) {
                values.put(key, displayName.getValue());
            }
        }
        for (final String symbol : sorted) {
            simpleQuote(regex, symbol).append('|');
        }
        return values;
    }

    /**
     * Get a cache of Strategies for a particular field
     *
     * @param field The Calendar field
     * @return a cache of Locale to Strategy
     */
    private static ConcurrentMap<Locale, Strategy> getCache(final int field) {
        synchronized (CACHES) {
            if (CACHES[field] == null) {
                CACHES[field] = new SafeConcurrentHashMap<>(3);
            }
            return CACHES[field];
        }
    }

    /**
     * Initialize derived fields from defining fields. This is called from constructor and from readObject (de-serialization)
     *
     * @param definingCalendar the {@link java.util.Calendar} instance used to initialize this FastDateParser
     */
    private void init(final Calendar definingCalendar) {
        patterns = new ArrayList<>();

        final StrategyParser fm = new StrategyParser(definingCalendar);
        for (; ; ) {
            final StrategyAndWidth field = fm.getNextStrategy();
            if (field == null) {
                break;
            }
            patterns.add(field);
        }
    }

    /**
     * Create the object after serialization. This implementation reinitializes the transient properties.
     *
     * @param in ObjectInputStream from which the object is being deserialized.
     * @throws IOException            if there is an IO issue.
     * @throws ClassNotFoundException if a class cannot be found.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);
        init(definingCalendar);
    }

    @Override
    public Date parse(final String source) throws DateException {
        final ParsePosition pp = new ParsePosition(0);
        final Date date = parse(source, pp);
        if (date == null) {
            // Add a note re supported date range
            if (locale.equals(JAPANESE_IMPERIAL)) {
                throw new DateException("(The " + locale + " locale does not support dates before 1868 AD)\n" +
                        "Unparseable date: \"" + source, pp.getErrorIndex());
            }
            throw new DateException("Unparseable date: " + source, pp.getErrorIndex());
        }
        return date;
    }

    @Override
    public Date parse(final String source, final ParsePosition pos) {
        // timing tests indicate getting new instance is 19% faster than cloning
        final Calendar cal = Calendar.getInstance(timeZone, locale);
        cal.clear();

        return parse(source, pos, cal) ? cal.getTime() : null;
    }

    @Override
    public boolean parse(final String source, final ParsePosition pos, final Calendar calendar) {
        final ListIterator<StrategyAndWidth> lt = patterns.listIterator();
        while (lt.hasNext()) {
            final StrategyAndWidth strategyAndWidth = lt.next();
            final int maxWidth = strategyAndWidth.getMaxWidth(lt);
            if (!strategyAndWidth.strategy.parse(this, calendar, source, pos, maxWidth)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 使用当前的世纪调整两位数年份为四位数年份
     *
     * @param twoDigitYear 两位数年份
     * @return A value between centuryStart(inclusive) to centuryStart+100(exclusive)
     */
    private int adjustYear(final int twoDigitYear) {
        final int trial = century + twoDigitYear;
        return twoDigitYear >= startYear ? trial : trial + 100;
    }

    /**
     * Obtain a Strategy given a field from a SimpleDateFormat pattern
     *
     * @param f                格式
     * @param width            长度
     * @param definingCalendar The calendar to obtain the short and long values
     * @return The Strategy that will handle parsing for the field
     */
    private Strategy getStrategy(final char f, final int width, final Calendar definingCalendar) {
        switch (f) {
            default:
                throw new IllegalArgumentException("Format '" + f + "' not supported");
            case 'D':
                return DAY_OF_YEAR_STRATEGY;
            case 'E':
                return getLocaleSpecificStrategy(Calendar.DAY_OF_WEEK, definingCalendar);
            case 'F':
                return DAY_OF_WEEK_IN_MONTH_STRATEGY;
            case 'G':
                return getLocaleSpecificStrategy(Calendar.ERA, definingCalendar);
            case 'H': // Hour in day (0-23)
                return HOUR_OF_DAY_STRATEGY;
            case 'K': // Hour in am/pm (0-11)
                return HOUR_STRATEGY;
            case 'M':
                return width >= 3 ? getLocaleSpecificStrategy(Calendar.MONTH, definingCalendar) : NUMBER_MONTH_STRATEGY;
            case 'S':
                return MILLISECOND_STRATEGY;
            case 'W':
                return WEEK_OF_MONTH_STRATEGY;
            case 'a':
                return getLocaleSpecificStrategy(Calendar.AM_PM, definingCalendar);
            case 'd':
                return DAY_OF_MONTH_STRATEGY;
            case 'h': // Hour in am/pm (1-12), i.e. midday/midnight is 12, not 0
                return HOUR12_STRATEGY;
            case 'k': // Hour in day (1-24), i.e. midnight is 24, not 0
                return HOUR24_OF_DAY_STRATEGY;
            case 'm':
                return MINUTE_STRATEGY;
            case 's':
                return SECOND_STRATEGY;
            case 'u':
                return DAY_OF_WEEK_STRATEGY;
            case 'w':
                return WEEK_OF_YEAR_STRATEGY;
            case 'y':
            case 'Y':
                return width > 2 ? LITERAL_YEAR_STRATEGY : ABBREVIATED_YEAR_STRATEGY;
            case 'X':
                return ISO8601TimeZoneStrategy.getStrategy(width);
            case 'Z':
                if (width == 2) {
                    return ISO8601TimeZoneStrategy.ISO_8601_3_STRATEGY;
                }
                //$FALL-THROUGH$
            case 'z':
                return getLocaleSpecificStrategy(Calendar.ZONE_OFFSET, definingCalendar);
        }
    }

    /**
     * Construct a Strategy that parses a Text field
     *
     * @param field            The Calendar field
     * @param definingCalendar The calendar to obtain the short and long values
     * @return a TextStrategy for the field and Locale
     */
    private Strategy getLocaleSpecificStrategy(final int field, final Calendar definingCalendar) {
        final ConcurrentMap<Locale, Strategy> cache = getCache(field);
        Strategy strategy = cache.get(locale);
        if (strategy == null) {
            strategy = field == Calendar.ZONE_OFFSET ? new TimeZoneStrategy(locale) : new CaseInsensitiveTextStrategy(field, definingCalendar, locale);
            final Strategy inCache = cache.putIfAbsent(locale, strategy);
            if (inCache != null) {
                return inCache;
            }
        }
        return strategy;
    }

    /**
     * Holds strategy and field width
     */
    private static class StrategyAndWidth {
        final Strategy strategy;
        final int width;

        StrategyAndWidth(final Strategy strategy, final int width) {
            this.strategy = strategy;
            this.width = width;
        }

        int getMaxWidth(final ListIterator<StrategyAndWidth> lt) {
            if (!strategy.isNumber() || !lt.hasNext()) {
                return 0;
            }
            final Strategy nextStrategy = lt.next().strategy;
            lt.previous();
            return nextStrategy.isNumber() ? width : 0;
        }
    }

    /**
     * 单个日期字段的分析策略
     */
    private static abstract class Strategy {
        /**
         * Is this field a number? The default implementation returns false.
         *
         * @return true, if field is a number
         */
        boolean isNumber() {
            return false;
        }

        abstract boolean parse(FastDateParser parser, Calendar calendar, String source, ParsePosition pos, int maxWidth);
    }

    /**
     * A strategy to parse a single field from the parsing pattern
     */
    private static abstract class PatternStrategy extends Strategy {

        private Pattern pattern;

        void createPattern(final StringBuilder regex) {
            createPattern(regex.toString());
        }

        void createPattern(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        boolean parse(final FastDateParser parser, final Calendar calendar, final String source, final ParsePosition pos, final int maxWidth) {
            final Matcher matcher = pattern.matcher(source.substring(pos.getIndex()));
            if (!matcher.lookingAt()) {
                pos.setErrorIndex(pos.getIndex());
                return false;
            }
            pos.setIndex(pos.getIndex() + matcher.end(1));
            setCalendar(parser, calendar, matcher.group(1));
            return true;
        }

        abstract void setCalendar(FastDateParser parser, Calendar cal, String value);
    }

    /**
     * A strategy that copies the static or quoted field in the parsing pattern
     */
    private static class CopyQuotedStrategy extends Strategy {

        final private String formatField;

        /**
         * Construct a Strategy that ensures the formatField has literal text
         *
         * @param formatField The literal text to match
         */
        CopyQuotedStrategy(final String formatField) {
            this.formatField = formatField;
        }

        @Override
        boolean parse(final FastDateParser parser, final Calendar calendar, final String source, final ParsePosition pos, final int maxWidth) {
            for (int idx = 0; idx < formatField.length(); ++idx) {
                final int sIdx = idx + pos.getIndex();
                if (sIdx == source.length()) {
                    pos.setErrorIndex(sIdx);
                    return false;
                }
                if (formatField.charAt(idx) != source.charAt(sIdx)) {
                    pos.setErrorIndex(sIdx);
                    return false;
                }
            }
            pos.setIndex(formatField.length() + pos.getIndex());
            return true;
        }
    }

    /**
     * A strategy that handles a text field in the parsing pattern
     */
    private static class CaseInsensitiveTextStrategy extends PatternStrategy {
        final Locale locale;
        private final int field;
        private final Map<String, Integer> lKeyValues;

        /**
         * Construct a Strategy that parses a Text field
         *
         * @param field            The Calendar field
         * @param definingCalendar The Calendar to use
         * @param locale           The Locale to use
         */
        CaseInsensitiveTextStrategy(final int field, final Calendar definingCalendar, final Locale locale) {
            this.field = field;
            this.locale = locale;

            final StringBuilder regex = new StringBuilder();
            regex.append("((?iu)");
            lKeyValues = appendDisplayNames(definingCalendar, locale, field, regex);
            regex.setLength(regex.length() - 1);
            regex.append(Symbol.PARENTHESE_RIGHT);
            createPattern(regex);
        }

        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            final Integer iVal = lKeyValues.get(value.toLowerCase(locale));
            cal.set(field, iVal);
        }
    }

    /**
     * A strategy that handles a number field in the parsing pattern
     */
    private static class NumberStrategy extends Strategy {
        private final int field;

        /**
         * Construct a Strategy that parses a Number field
         *
         * @param field The Calendar field
         */
        NumberStrategy(final int field) {
            this.field = field;
        }

        @Override
        boolean isNumber() {
            return true;
        }

        @Override
        boolean parse(final FastDateParser parser, final Calendar calendar, final String source, final ParsePosition pos, final int maxWidth) {
            int idx = pos.getIndex();
            int last = source.length();

            if (maxWidth == 0) {
                // if no maxWidth, strip leading white space
                for (; idx < last; ++idx) {
                    final char c = source.charAt(idx);
                    if (!Character.isWhitespace(c)) {
                        break;
                    }
                }
                pos.setIndex(idx);
            } else {
                final int end = idx + maxWidth;
                if (last > end) {
                    last = end;
                }
            }

            for (; idx < last; ++idx) {
                final char c = source.charAt(idx);
                if (!Character.isDigit(c)) {
                    break;
                }
            }

            if (pos.getIndex() == idx) {
                pos.setErrorIndex(idx);
                return false;
            }

            final int value = Integer.parseInt(source.substring(pos.getIndex(), idx));
            pos.setIndex(idx);

            calendar.set(field, modify(parser, value));
            return true;
        }

        /**
         * Make any modifications to parsed integer
         *
         * @param parser The parser
         * @param iValue The parsed integer
         * @return The modified value
         */
        int modify(final FastDateParser parser, final int iValue) {
            return iValue;
        }

    }

    /**
     * A strategy that handles a timezone field in the parsing pattern
     */
    static class TimeZoneStrategy extends PatternStrategy {
        private static final String RFC_822_TIME_ZONE = "[+-]\\d{4}";
        private static final String UTC_TIME_ZONE_WITH_OFFSET = "[+-]\\d{2}:\\d{2}";
        private static final String GMT_OPTION = "GMT[+-]\\d{1,2}:\\d{2}";
        /**
         * Index of zone data
         */
        private static final int ID = 0;
        private final Locale locale;
        private final Map<String, TzInfo> tzNames = new HashMap<>();

        /**
         * Construct a Strategy that parses a TimeZone
         *
         * @param locale The Locale
         */
        TimeZoneStrategy(final Locale locale) {
            this.locale = locale;

            final StringBuilder sb = new StringBuilder();
            sb.append("((?iu)" + RFC_822_TIME_ZONE + "|" + UTC_TIME_ZONE_WITH_OFFSET + "|" + GMT_OPTION);

            final Set<String> sorted = new TreeSet<>(LONGER_FIRST_LOWERCASE);

            final String[][] zones = DateFormatSymbols.getInstance(locale).getZoneStrings();
            for (final String[] zoneNames : zones) {
                // offset 0 is the time zone ID and is not localized
                final String tzId = zoneNames[ID];
                if ("GMT".equalsIgnoreCase(tzId)) {
                    continue;
                }
                final TimeZone tz = TimeZone.getTimeZone(tzId);
                // offset 1 is long standard name
                // offset 2 is short standard name
                final TzInfo standard = new TzInfo(tz, false);
                TzInfo tzInfo = standard;
                for (int i = 1; i < zoneNames.length; ++i) {
                    switch (i) {
                        case 3: // offset 3 is long daylight savings (or summertime) name
                            // offset 4 is the short summertime name
                            tzInfo = new TzInfo(tz, true);
                            break;
                        case 5: // offset 5 starts additional names, probably standard time
                            tzInfo = standard;
                            break;
                    }
                    if (zoneNames[i] != null) {
                        final String key = zoneNames[i].toLowerCase(locale);
                        // ignore the data associated with duplicates supplied in
                        // the additional names
                        if (sorted.add(key)) {
                            tzNames.put(key, tzInfo);
                        }
                    }
                }
            }
            // order the regex alternatives with longer strings first, greedy
            // match will ensure longest string will be consumed
            for (final String zoneName : sorted) {
                simpleQuote(sb.append('|'), zoneName);
            }
            sb.append(Symbol.PARENTHESE_RIGHT);
            createPattern(sb);
        }

        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            if (value.charAt(0) == Symbol.C_PLUS || value.charAt(0) == Symbol.C_MINUS) {
                final TimeZone tz = TimeZone.getTimeZone("GMT" + value);
                cal.setTimeZone(tz);
            } else if (value.regionMatches(true, 0, "GMT", 0, 3)) {
                final TimeZone tz = TimeZone.getTimeZone(value.toUpperCase());
                cal.setTimeZone(tz);
            } else {
                final TzInfo tzInfo = tzNames.get(value.toLowerCase(locale));
                cal.set(Calendar.DST_OFFSET, tzInfo.dstOffset);
                // cal.set(Calendar.ZONE_OFFSET, tzInfo.zone.getRawOffset());
                cal.set(Calendar.ZONE_OFFSET, parser.getTimeZone().getRawOffset());
            }
        }

        private static class TzInfo {
            TimeZone zone;
            int dstOffset;

            TzInfo(final TimeZone tz, final boolean useDst) {
                zone = tz;
                dstOffset = useDst ? tz.getDSTSavings() : 0;
            }
        }
    }

    private static class ISO8601TimeZoneStrategy extends PatternStrategy {
        // Z, +hh, -hh, +hhmm, -hhmm, +hh:mm or -hh:mm

        private static final Strategy ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
        private static final Strategy ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
        private static final Strategy ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::)\\d{2}))");

        /**
         * Construct a Strategy that parses a TimeZone
         *
         * @param pattern The Pattern
         */
        ISO8601TimeZoneStrategy(final String pattern) {
            createPattern(pattern);
        }

        /**
         * Factory method for ISO8601TimeZoneStrategies.
         *
         * @param tokenLen a token indicating the length of the TimeZone String to be formatted.
         * @return a ISO8601TimeZoneStrategy that can format TimeZone String of length {@code tokenLen}. If no such strategy exists, an IllegalArgumentException will be thrown.
         */
        static Strategy getStrategy(final int tokenLen) {
            switch (tokenLen) {
                case 1:
                    return ISO_8601_1_STRATEGY;
                case 2:
                    return ISO_8601_2_STRATEGY;
                case 3:
                    return ISO_8601_3_STRATEGY;
                default:
                    throw new IllegalArgumentException("invalid number of X");
            }
        }

        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            if (Objects.equals(value, "Z")) {
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                cal.setTimeZone(TimeZone.getTimeZone("GMT" + value));
            }
        }
    }

    /**
     * Parse format into Strategies
     */
    private class StrategyParser {
        final private Calendar definingCalendar;
        private int currentIdx;

        StrategyParser(final Calendar definingCalendar) {
            this.definingCalendar = definingCalendar;
        }

        StrategyAndWidth getNextStrategy() {
            if (currentIdx >= pattern.length()) {
                return null;
            }

            final char c = pattern.charAt(currentIdx);
            if (isFormatLetter(c)) {
                return letterPattern(c);
            }
            return literal();
        }

        private StrategyAndWidth letterPattern(final char c) {
            final int begin = currentIdx;
            while (++currentIdx < pattern.length()) {
                if (pattern.charAt(currentIdx) != c) {
                    break;
                }
            }

            final int width = currentIdx - begin;
            return new StrategyAndWidth(getStrategy(c, width, definingCalendar), width);
        }

        private StrategyAndWidth literal() {
            boolean activeQuote = false;

            final StringBuilder sb = new StringBuilder();
            while (currentIdx < pattern.length()) {
                final char c = pattern.charAt(currentIdx);
                if (!activeQuote && isFormatLetter(c)) {
                    break;
                } else if (c == '\'' && (++currentIdx == pattern.length() || pattern.charAt(currentIdx) != '\'')) {
                    activeQuote = !activeQuote;
                    continue;
                }
                ++currentIdx;
                sb.append(c);
            }

            if (activeQuote) {
                throw new IllegalArgumentException("Unterminated quote");
            }

            final String formatField = sb.toString();
            return new StrategyAndWidth(new CopyQuotedStrategy(formatField), formatField.length());
        }
    }

}
