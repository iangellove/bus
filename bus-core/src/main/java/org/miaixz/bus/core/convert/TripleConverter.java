/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.core.convert;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.TypeKit;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * {@link Triplet} 转换器，支持以下类型转为Triple：
 * <ul>
 *     <li>Bean，包含{@code getLeft}、{@code getMiddle}和{@code getRight}方法</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TripleConverter implements Converter {

    /**
     * 单例
     */
    public static final TripleConverter INSTANCE = new TripleConverter();

    /**
     * Map转Entry
     *
     * @param leftType  键类型
     * @param rightType 值类型
     * @param map       被转换的map
     * @return Entry
     */
    private static Triplet<?, ?, ?> mapToTriple(final Type leftType, final Type middleType, final Type rightType, final Map map) {

        final Object left = map.get("left");
        final Object middle = map.get("middle");
        final Object right = map.get("right");

        final CompositeConverter convert = CompositeConverter.getInstance();
        return Triplet.of(
                TypeKit.isUnknown(leftType) ? left : convert.convert(leftType, left),
                TypeKit.isUnknown(middleType) ? middle : convert.convert(middleType, middle),
                TypeKit.isUnknown(rightType) ? right : convert.convert(rightType, right)
        );
    }

    @Override
    public Object convert(Type targetType, final Object value) throws ConvertException {
        if (targetType instanceof TypeReference) {
            targetType = ((TypeReference<?>) targetType).getType();
        }
        final Type leftType = TypeKit.getTypeArgument(targetType, 0);
        final Type middileType = TypeKit.getTypeArgument(targetType, 1);
        final Type rightType = TypeKit.getTypeArgument(targetType, 2);

        return convert(leftType, middileType, rightType, value);
    }

    /**
     * 转换对象为指定键值类型的指定类型Map
     *
     * @param leftType   键类型
     * @param middleType 中值类型
     * @param rightType  值类型
     * @param value      被转换的值
     * @return 转换后的Map
     * @throws ConvertException 转换异常或不支持的类型
     */
    public Triplet<?, ?, ?> convert(final Type leftType, final Type middleType, final Type rightType, final Object value)
            throws ConvertException {
        Map map = null;
        if (BeanKit.isReadableBean(value.getClass())) {
            map = BeanKit.beanToMap(value);
        }

        if (null != map) {
            return mapToTriple(leftType, middleType, rightType, map);
        }

        throw new ConvertException("Unsupported to map from [{}] of type: {}", value, value.getClass().getName());
    }

}
