/**
 * 加密解密模块，实现了对JDK中加密解密算法的封装。
 * 入口为Builder或加密算法类，例如：
 * <pre>
 *   Builder.md5(); 或 MD5 md5 = new MD5();
 * </pre>
 * <pre>
 *
 * 1. 对称加密（symmetric），例如：AES、DES等
 * 2. 非对称加密（asymmetric），例如：RSA、DSA等
 * 3. 摘要加密（digest），例如：MD5、SHA-1、SHA-256、HMAC等
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.crypto;
