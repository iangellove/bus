/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
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
package org.miaixz.bus.oauth;

import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.metric.DefaultProvider;

/**
 * OAuth平台的API地址的统一接口，提供以下方法：
 * 1) {@link Complex#authorize()}: 获取授权url. 必须实现
 * 2) {@link Complex#accessToken()}: 获取accessToken的url. 必须实现
 * 3) {@link Complex#userInfo()}: 获取用户信息的url. 必须实现
 * 4) {@link Complex#revoke()}: 获取取消授权的url. 非必须实现接口（部分平台不支持）
 * 5) {@link Complex#refresh()}: 获取刷新授权的url. 非必须实现接口（部分平台不支持）
 * 注：
 * ①、如需通过扩展实现第三方授权，请参考{@link Registry}自行创建对应的枚举类并实现{@link Complex}接口
 * ②、如果不是使用的枚举类，那么在授权成功后获取用户信息时，需要单独处理source字段的赋值
 * ③、如果扩展了对应枚举类时，在{@link Provider#login(Callback)}中可以通过{@code xx.toString()}获取对应的source
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Complex {

    /**
     * 授权的api
     *
     * @return url
     */
    String authorize();

    /**
     * 获取accessToken的api
     *
     * @return url
     */
    String accessToken();

    /**
     * 获取用户信息的api
     *
     * @return url
     */
    String userInfo();

    /**
     * 取消授权的api
     *
     * @return url
     */
    default String revoke() {
        throw new AuthorizedException(ErrorCode.UNSUPPORTED.getCode());
    }

    /**
     * 刷新授权的api
     *
     * @return url
     */
    default String refresh() {
        throw new AuthorizedException(ErrorCode.UNSUPPORTED.getCode());
    }

    /**
     * 获取Source的字符串名字
     *
     * @return name
     */
    default String getName() {
        if (this instanceof Enum) {
            return String.valueOf(this);
        }
        return this.getClass().getSimpleName();
    }

    /**
     * 平台对应的 AuthorizeProvider 实现类，必须继承自 {@link DefaultProvider}
     *
     * @return class
     */
    Class<? extends DefaultProvider> getTargetClass();

}
