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
package org.miaixz.bus.core.net.tls;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Protocol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

import javax.net.ssl.*;
import java.security.*;
import java.util.Arrays;

/**
 * {@link SSLContext}构建器，可以自定义：
 * <ul>
 *     <li>协议（protocol），默认TLS</li>
 *     <li>{@link KeyManager}，默认空</li>
 *     <li>{@link TrustManager}，默认{@link TrustAnyTrustManager}，即信任全部</li>
 *     <li>{@link SecureRandom}</li>
 * </ul>
 * <p>
 * 构建后可获得{@link SSLContext}，通过调用{@link SSLContext#getSocketFactory()}获取{@link javax.net.ssl.SSLSocketFactory}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SSLContextBuilder implements Builder<SSLContext> {

    private static final long serialVersionUID = -1L;

    private String protocol = Protocol.TLS;
    private KeyManager[] keyManagers;
    private TrustManager[] trustManagers = TrustAnyTrustManager.TRUST_ANYS;
    private SecureRandom secureRandom = new SecureRandom();


    /**
     * 创建 SSLContextBuilder
     *
     * @return SSLContextBuilder
     */
    public static SSLContextBuilder of() {
        return new SSLContextBuilder();
    }

    /**
     * 获取默认的{@link SSLContext}
     *
     * @return {@link SSLContext}
     */
    public static SSLContext getDefault() {
        try {
            return SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建{@link SSLContext}，信任全部，协议为TLS
     *
     * @return {@link SSLContext}
     * @throws InternalException 包装 GeneralSecurityException异常
     */
    public static SSLContext createTrustAnySSLContext() throws InternalException {
        return createTrustAnySSLContext(null);
    }

    /**
     * 创建{@link SSLContext}，信任全部
     *
     * @param protocol SSL协议，例如TLS等，{@code null}表示默认TLS
     * @return {@link SSLContext}
     * @throws InternalException 包装 GeneralSecurityException异常
     */
    public static SSLContext createTrustAnySSLContext(final String protocol) throws InternalException {
        return of()
                .setProtocol(protocol)
                // 信任所有服务端
                .setTrustManagers(TrustAnyTrustManager.TRUST_ANYS)
                .build();
    }

    /**
     * 创建{@link SSLContext}
     *
     * @param protocol     SSL协议，例如TLS等
     * @param keyManager   密钥管理器,{@code null}表示默认
     * @param trustManager 信任管理器, {@code null}表示默认
     * @return {@link SSLContext}
     * @throws InternalException 包装 GeneralSecurityException异常
     */
    public static SSLContext createSSLContext(final String protocol, final KeyManager keyManager, final TrustManager trustManager)
            throws InternalException {
        return createSSLContext(protocol,
                keyManager == null ? null : new KeyManager[]{keyManager},
                trustManager == null ? null : new TrustManager[]{trustManager});
    }

    /**
     * 创建和初始化{@link SSLContext}
     *
     * @param protocol      SSL协议，例如TLS等
     * @param keyManagers   密钥管理器,{@code null}表示默认
     * @param trustManagers 信任管理器, {@code null}表示默认
     * @return {@link SSLContext}
     * @throws InternalException 包装 GeneralSecurityException异常
     */
    public static SSLContext createSSLContext(final String protocol, final KeyManager[] keyManagers, final TrustManager[] trustManagers) throws InternalException {
        return of()
                .setProtocol(protocol)
                .setKeyManagers(keyManagers)
                .setTrustManagers(trustManagers).build();
    }

    /**
     * 创建SSL证书
     *
     * @param x509TrustManager 证书信息
     * @return SSLSocketFactory ssl socket工厂
     */
    public static SSLSocketFactory newSslSocketFactory(X509TrustManager x509TrustManager) {
        try {
            SSLContext sslContext = getSSLContext();
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError("No System TLS", e); // The system has no TLS. Just give up.
        }
    }

    public static X509TrustManager newTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {
            throw new AssertionError("No System TLS", e); // The system has no TLS. Just give up.
        }
    }

    public static SSLContext getSSLContext() {
        try {
            return SSLContext.getInstance(Http.TLS_V_13);
        } catch (NoSuchAlgorithmException e) {
            try {
                return SSLContext.getInstance(Http.TLS);
            } catch (NoSuchAlgorithmException e2) {
                throw new IllegalStateException("No TLS provider", e);
            }
        }
    }

    /**
     * 设置协议。例如TLS等
     *
     * @param protocol 协议
     * @return 自身
     */
    public SSLContextBuilder setProtocol(final String protocol) {
        if (StringKit.isNotBlank(protocol)) {
            this.protocol = protocol;
        }
        return this;
    }

    /**
     * 设置信任信息
     *
     * @param trustManagers TrustManager列表
     * @return 自身
     */
    public SSLContextBuilder setTrustManagers(final TrustManager... trustManagers) {
        if (ArrayKit.isNotEmpty(trustManagers)) {
            this.trustManagers = trustManagers;
        }
        return this;
    }

    /**
     * 设置 JSSE data managers
     *
     * @param keyManagers JSSE data managers
     * @return 自身
     */
    public SSLContextBuilder setKeyManagers(final KeyManager... keyManagers) {
        if (ArrayKit.isNotEmpty(keyManagers)) {
            this.keyManagers = keyManagers;
        }
        return this;
    }

    /**
     * 设置 SecureRandom
     *
     * @param secureRandom SecureRandom
     * @return 自己
     */
    public SSLContextBuilder setSecureRandom(final SecureRandom secureRandom) {
        if (null != secureRandom) {
            this.secureRandom = secureRandom;
        }
        return this;
    }

    /**
     * 构建{@link SSLContext}
     *
     * @return {@link SSLContext}
     */
    @Override
    public SSLContext build() {
        return buildQuietly();
    }

    /**
     * 构建{@link SSLContext}需要处理异常
     *
     * @return {@link SSLContext}
     * @throws NoSuchAlgorithmException 无此算法异常
     * @throws KeyManagementException   密钥管理异常
     */
    public SSLContext buildChecked() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(this.keyManagers, this.trustManagers, this.secureRandom);
        return sslContext;
    }

    /**
     * 构建{@link SSLContext}
     *
     * @return {@link SSLContext}
     * @throws InternalException 包装 GeneralSecurityException异常
     */
    public SSLContext buildQuietly() throws InternalException {
        try {
            return buildChecked();
        } catch (final GeneralSecurityException e) {
            throw new InternalException(e);
        }
    }

}
