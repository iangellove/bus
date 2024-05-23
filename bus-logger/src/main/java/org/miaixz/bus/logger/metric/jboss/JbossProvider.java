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
package org.miaixz.bus.logger.metric.jboss;

import org.jboss.logging.Logger;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.Provider;

/**
 * jboss-logging
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JbossProvider extends Provider {

    private static final long serialVersionUID = -1L;

    /**
     * 日志门面
     */
    private final transient Logger logger;

    /**
     * 构造
     *
     * @param logger 日志对象
     */
    public JbossProvider(final Logger logger) {
        this.logger = logger;
    }

    /**
     * 构造
     *
     * @param clazz 日志实现类
     */
    public JbossProvider(final Class<?> clazz) {
        this((null == clazz) ? Normal.NULL : clazz.getName());
    }

    /**
     * 构造
     *
     * @param name 日志实现类名
     */
    public JbossProvider(final String name) {
        this(Logger.getLogger(name));
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTrace() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isTrace()) {
            logger.trace(fqcn, StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isDebug() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isDebug()) {
            logger.debug(fqcn, StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isInfo() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isInfo()) {
            logger.info(fqcn, StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isWarn() {
        return logger.isEnabled(Logger.Level.WARN);
    }

    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isWarn()) {
            logger.warn(fqcn, StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isError() {
        return logger.isEnabled(Logger.Level.ERROR);
    }

    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isError()) {
            logger.error(fqcn, StringKit.format(format, args), t);
        }
    }

    @Override
    public void log(final String fqcn, final Level level, final Throwable t, final String format, final Object... args) {
        switch (level) {
            case TRACE:
                trace(fqcn, t, format, args);
                break;
            case DEBUG:
                debug(fqcn, t, format, args);
                break;
            case INFO:
                info(fqcn, t, format, args);
                break;
            case WARN:
                warn(fqcn, t, format, args);
                break;
            case ERROR:
                error(fqcn, t, format, args);
                break;
            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

}
