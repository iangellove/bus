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
package org.miaixz.bus.oauth.metric.afdian;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 爱发电 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AfDianProvider extends DefaultProvider {

    public AfDianProvider(Context context) {
        super(context, Registry.AFDIAN);
    }

    public AfDianProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.AFDIAN, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        params.put("code", authCallback.getCode());
        params.put("redirect_uri", context.getRedirectUri());

        String response = Httpx.post(Registry.AFDIAN.accessToken(), params);

        JSONObject accessTokenObject = JSONObject.parseObject(response);
        String userId = accessTokenObject.getJSONObject("data").getString("user_id");
        return AccToken.builder().userId(userId).build();
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        return Property.builder()
                .uuid(accToken.getUserId())
                .gender(Gender.UNKNOWN)
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize())
                .queryParam("response_type", "code")
                .queryParam("scope", "basic")
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

}
