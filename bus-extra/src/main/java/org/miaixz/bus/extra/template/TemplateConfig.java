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
package org.miaixz.bus.extra.template;

import org.miaixz.bus.core.lang.Charset;

import java.io.Serializable;
import java.util.Objects;

/**
 * 模板配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TemplateConfig implements Serializable {

    /**
     * 默认配置
     */
    public static final TemplateConfig DEFAULT = new TemplateConfig();

    private static final long serialVersionUID = -1L;
    /**
     * 编码
     */
    private java.nio.charset.Charset charset;
    /**
     * 模板路径，如果ClassPath或者WebRoot模式，则表示相对路径
     */
    private String path;
    /**
     * 模板资源加载方式
     */
    private ResourceMode resourceMode;
    /**
     * 自定义引擎，当多个jar包引入时，可以自定使用的默认引擎
     */
    private Class<? extends TemplateProvider> customEngine;

    /**
     * 默认构造，使用UTF8编码，默认从ClassPath获取模板
     */
    public TemplateConfig() {
        this(null);
    }

    /**
     * 构造，默认UTF-8编码
     *
     * @param path 模板路径，如果ClassPath或者WebRoot模式，则表示相对路径
     */
    public TemplateConfig(final String path) {
        this(path, ResourceMode.STRING);
    }

    /**
     * 构造，默认UTF-8编码
     *
     * @param path         模板路径，如果ClassPath或者WebRoot模式，则表示相对路径
     * @param resourceMode 模板资源加载方式
     */
    public TemplateConfig(final String path, final ResourceMode resourceMode) {
        this(Charset.UTF_8, path, resourceMode);
    }

    /**
     * 构造
     *
     * @param charset      编码
     * @param path         模板路径，如果ClassPath或者WebRoot模式，则表示相对路径
     * @param resourceMode 模板资源加载方式
     */
    public TemplateConfig(final java.nio.charset.Charset charset, final String path, final ResourceMode resourceMode) {
        this.charset = charset;
        this.path = path;
        this.resourceMode = resourceMode;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     */
    public void setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public String getCharsetString() {
        if (null == this.charset) {
            return null;
        }
        return this.charset.toString();
    }

    /**
     * 获取模板路径，如果ClassPath或者WebRoot模式，则表示相对路径
     *
     * @return 模板路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置模板路径，如果ClassPath或者WebRoot模式，则表示相对路径
     *
     * @param path 模板路径
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * 获取模板资源加载方式
     *
     * @return 模板资源加载方式
     */
    public ResourceMode getResourceMode() {
        return resourceMode;
    }

    /**
     * 设置模板资源加载方式
     *
     * @param resourceMode 模板资源加载方式
     */
    public void setResourceMode(final ResourceMode resourceMode) {
        this.resourceMode = resourceMode;
    }

    /**
     * 获取自定义引擎，null表示系统自动判断
     *
     * @return 自定义引擎，null表示系统自动判断
     */
    public Class<? extends TemplateProvider> getCustomEngine() {
        return customEngine;
    }


    /**
     * 设置自定义引擎，null表示系统自动判断
     *
     * @param customEngine 自定义引擎，null表示系统自动判断
     * @return this
     */
    public TemplateConfig setCustomEngine(final Class<? extends TemplateProvider> customEngine) {
        this.customEngine = customEngine;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TemplateConfig that = (TemplateConfig) o;
        return Objects.equals(charset, that.charset) &&
                Objects.equals(path, that.path) &&
                resourceMode == that.resourceMode &&
                Objects.equals(customEngine, that.customEngine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charset, path, resourceMode, customEngine);
    }

    /**
     * 资源加载方式枚举
     */
    public enum ResourceMode {
        /**
         * 从ClassPath加载模板
         */
        CLASSPATH,
        /**
         * 从File目录加载模板
         */
        FILE,
        /**
         * 从WebRoot目录加载模板
         */
        WEB_ROOT,
        /**
         * 从模板文本加载模板
         */
        STRING,
        /**
         * 复合加载模板（分别从File、ClassPath、Web-root、String方式尝试查找模板）
         */
        COMPOSITE
    }
}
