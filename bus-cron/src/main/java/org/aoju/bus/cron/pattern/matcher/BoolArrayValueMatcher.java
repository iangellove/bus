/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cron.pattern.matcher;

import org.aoju.bus.core.utils.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 将表达式中的数字值列表转换为Boolean数组,匹配时匹配相应数组位
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class BoolArrayValueMatcher implements ValueMatcher {

    boolean[] bValues;

    public BoolArrayValueMatcher(List<Integer> intValueList) {
        bValues = new boolean[Collections.max(intValueList) + 1];
        for (Integer value : intValueList) {
            bValues[value] = true;
        }
    }

    @Override
    public boolean match(Integer value) {
        if (null == value || value >= bValues.length) {
            return false;
        }
        return bValues[value];
    }

    @Override
    public String toString() {
        return StringUtils.format("Matcher:{}", this.bValues);
    }
}
