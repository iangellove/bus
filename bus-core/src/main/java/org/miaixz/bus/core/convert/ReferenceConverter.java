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
package org.miaixz.bus.core.convert;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.TypeKit;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

/**
 * {@link Reference}转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReferenceConverter extends AbstractConverter {

    private static final long serialVersionUID = -1L;

    public static ReferenceConverter INSTANCE = new ReferenceConverter();

    @Override
    protected Reference<?> convertInternal(final Class<?> targetClass, final Object value) {
        // 尝试将值转换为Reference泛型的类型
        Object targetValue = null;
        final Type paramType = TypeKit.getTypeArgument(targetClass);
        if (!TypeKit.isUnknown(paramType)) {
            targetValue = CompositeConverter.getInstance().convert(paramType, value);
        }
        if (null == targetValue) {
            targetValue = value;
        }

        if (targetClass == WeakReference.class) {
            return new WeakReference(targetValue);
        } else if (targetClass == SoftReference.class) {
            return new SoftReference(targetValue);
        }

        throw new UnsupportedOperationException(StringKit.format("Unsupport Reference type: {}", targetClass.getName()));
    }

}
