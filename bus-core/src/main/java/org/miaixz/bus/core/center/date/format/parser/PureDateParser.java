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

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.center.date.printer.DefaultDatePrinter;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.MathKit;

/**
 * 纯数字的日期字符串解析，支持格式包括；
 * <ul>
 *   <li>yyyyMMddHHmmss</li>
 *   <li>yyyyMMddHHmmssSSS</li>
 *   <li>yyyyMMdd</li>
 *   <li>HHmmss</li>
 *   <li>毫秒时间戳</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PureDateParser extends DefaultDatePrinter implements PredicateDateParser {

    private static final long serialVersionUID = -1L;

    /**
     * 单例
     */
    public static PureDateParser INSTANCE = new PureDateParser();

    @Override
    public boolean test(final CharSequence dateStr) {
        return MathKit.isNumber(dateStr);
    }

    @Override
    public DateTime parse(final String source) throws DateException {
        final int length = source.length();
        // 纯数字形式
        if (length == Fields.PURE_DATETIME.length()) {
            return new DateTime(source, Formatter.PURE_DATETIME_FORMAT);
        } else if (length == Fields.PURE_DATETIME_MS.length()) {
            return new DateTime(source, Formatter.PURE_DATETIME_MS_FORMAT);
        } else if (length == Fields.PURE_DATE.length()) {
            return new DateTime(source, Formatter.PURE_DATE_FORMAT);
        } else if (length == Fields.PURE_TIME.length()) {
            return new DateTime(source, Formatter.PURE_TIME_FORMAT);
        } else if (length >= 11 && length <= 13) {
            // 时间戳
            return new DateTime(MathKit.parseLong(source));
        }

        throw new DateException("No pure format fit for date String [{}] !", source);
    }

}
