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
package org.miaixz.bus.notify.metric.upyun;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.notify.magic.Property;

/**
 * 又拍云短信
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class UpyunProperty extends Property {

    /**
     * token
     */
    private String token;


    /**
     * 手机号发送短信的结果
     */
    @Getter
    @Setter
    public static class MessageId {

        /**
         * 错误情况
         */
        private String error_code;

        /**
         * 旧版本国内短信的 message 编号.
         */
        private Integer message_id;

        /**
         * message 编号
         */
        private String msg_id;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 判断是否成功
         *
         * @return 是否成功
         */
        public boolean succeed() {
            return StringKit.isBlank(error_code) && StringKit.isNotBlank(mobile);
        }

    }

}
