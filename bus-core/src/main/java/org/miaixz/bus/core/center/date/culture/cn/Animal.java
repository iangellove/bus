/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
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
package org.miaixz.bus.core.center.date.culture.cn;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.star.twentyeight.TwentyEightStar;

/**
 * 动物
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Animal extends Samsara {

    public static final String[] NAMES = {
            "蛟", "龙", "貉", "兔", "狐", "虎", "豹", "獬", "牛", "蝠",
            "鼠", "燕", "猪", "獝", "狼", "狗", "彘", "鸡", "乌", "猴",
            "猿", "犴", "羊", "獐", "马", "鹿", "蛇", "蚓"
    };

    public Animal(int index) {
        super(NAMES, index);
    }

    public Animal(String name) {
        super(NAMES, name);
    }

    public static Animal fromIndex(int index) {
        return new Animal(index);
    }

    public static Animal fromName(String name) {
        return new Animal(name);
    }

    public Animal next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 二十八宿
     *
     * @return 二十八宿
     */
    public TwentyEightStar getTwentyEightStar() {
        return TwentyEightStar.fromIndex(index);
    }

}
