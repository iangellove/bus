/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
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
package org.miaixz.bus.mapper.annotation;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.mapper.ORDER;
import org.miaixz.bus.mapper.Registry;
import org.miaixz.bus.mapper.support.GenId;
import org.miaixz.bus.mapper.support.GenSql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主键策略，用于替换 JPA 中的复杂用法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KeySql {

    /**
     * 是否使用 JDBC 方式获取主键，优先级最高，设置为 true 后，不对其他配置校验
     *
     * @return the boolean
     */
    boolean useGeneratedKeys() default false;

    /**
     * 优先级第二，根据配置的数据库类型取回主键，忽略其他配置
     *
     * @return the object
     */
    Registry dialect() default Registry.NULL;

    /**
     * 取主键的 SQL
     *
     * @return the string
     */
    String sql() default Normal.EMPTY;

    /**
     * 生成 SQL，初始化时执行，优先级低于 sql
     *
     * @return the class
     */
    Class<? extends GenSql> genSql() default GenSql.NULL.class;

    /**
     * 和 sql 可以配合使用，默认使用全局配置中的 ORDER
     *
     * @return the object
     */
    ORDER order() default ORDER.DEFAULT;

    /**
     * Java 方式生成主键，可以和发号器一类的服务配合使用
     *
     * @return the class
     */
    Class<? extends GenId> genId() default GenId.NULL.class;

}
