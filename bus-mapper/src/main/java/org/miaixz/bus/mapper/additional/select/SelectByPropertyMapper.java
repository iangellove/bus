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
package org.miaixz.bus.mapper.additional.select;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.mapper.annotation.RegisterMapper;

import java.util.List;

/**
 * 根据属性查询接口
 *
 * @param <T> 泛型
 * @author Kimi Liu
 * @since Java 17+
 */
@RegisterMapper
public interface SelectByPropertyMapper<T> {

    /**
     * 根据属性及对应值进行查询，只能有一个返回值，有多个结果时抛出异常，查询条件使用等号
     *
     * @param fn    查询属性
     * @param value 属性值
     * @return the object
     */
    @SelectProvider(type = SelectPropertyProvider.class, method = "dynamicSQL")
    T selectOneByProperty(@Param("fn") FunctionX<T, ?> fn, @Param("value") Object value);

    /**
     * 根据属性及对应值进行查询，有多个返回值，查询条件使用等号
     *
     * @param fn    查询属性
     * @param value 属性值
     * @return the list
     */
    @SelectProvider(type = SelectPropertyProvider.class, method = "dynamicSQL")
    List<T> selectByProperty(@Param("fn") FunctionX<T, ?> fn, @Param("value") Object value);

    /**
     * 根据属性及对应值进行查询，查询条件使用 in
     *
     * @param fn     查询属性
     * @param values 属性值集合，集合不能空
     * @return the list
     */
    @SelectProvider(type = SelectPropertyProvider.class, method = "dynamicSQL")
    List<T> selectInByProperty(@Param("fn") FunctionX<T, ?> fn, @Param("values") List<?> values);

    /**
     * 根据属性及对应值进行查询，查询条件使用 between
     *
     * @param fn    查询属性
     * @param begin 开始值
     * @param end   开始值
     * @return the list
     */
    @SelectProvider(type = SelectPropertyProvider.class, method = "dynamicSQL")
    List<T> selectBetweenByProperty(@Param("fn") FunctionX<T, ?> fn, @Param("begin") Object begin, @Param("end") Object end);

    /**
     * 根据属性及对应值进行查询，检查是否存在对应记录，查询条件使用等号
     *
     * @param fn    查询属性
     * @param value 属性值
     * @return the boolean
     */
    @SelectProvider(type = SelectPropertyProvider.class, method = "dynamicSQL")
    boolean existsWithProperty(@Param("fn") FunctionX<T, ?> fn, @Param("value") Object value);

    /**
     * 根据属性及对应值进行查询，统计符合条件的记录数，查询条件使用等号
     *
     * @param fn    查询属性
     * @param value 属性值
     * @return the int
     */
    @SelectProvider(type = SelectPropertyProvider.class, method = "dynamicSQL")
    int selectCountByProperty(@Param("fn") FunctionX<T, ?> fn, @Param("value") Object value);

}
