/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.crypto.digest.mac;

import org.aoju.bus.core.toolkit.IoKit;

import java.io.InputStream;

/**
 * MAC(Message Authentication Code)算法引擎
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public interface MacEngine {

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度，不足1使用 {@link  IoKit#DEFAULT_BUFFER_SIZE} 做为默认值
     * @return 摘要bytes
     */
    byte[] digest(InputStream data, int bufferLength);


    /**
     * 获取MAC算法块大小
     *
     * @return MAC算法块大小
     */
    int getMacLength();

    /**
     * 获取当前算法
     *
     * @return 算法
     */
    String getAlgorithm();

}
