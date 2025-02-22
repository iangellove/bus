package org.miaixz.bus.pay.metric.jdpay.models;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pay.magic.Property;
import org.miaixz.bus.pay.metric.jdpay.JdPayKit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 公用方法
 */
public class JdPayEntity extends Property {

    /**
     * 将建构的 builder 转为 Map
     *
     * @return 转化后的 Map
     */
    public Map<String, String> toMap() {
        String[] fieldNames = getFiledNames(this);
        HashMap<String, String> map = new HashMap<>(fieldNames.length);
        for (String name : fieldNames) {
            String value = (String) getFieldValueByName(name, this);
            if (StringKit.isNotEmpty(value)) {
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * 获取属性名数组
     *
     * @param object 对象
     * @return 返回对象属性名数组
     */
    public String[] getFiledNames(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    /**
     * 根据属性名获取属性值
     *
     * @param fieldName 属性名称
     * @param object    对象
     * @return 返回对应属性的值
     */
    public Object getFieldValueByName(String fieldName, Object object) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = new StringBuffer().append("get")
                    .append(firstLetter)
                    .append(fieldName.substring(1))
                    .toString();
            Method method = object.getClass().getMethod(getter);
            return method.invoke(object);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 自动生成请求接口的 xml
     *
     * @param rsaPrivateKey RSA 私钥
     * @param strDesKey     DES 密钥
     * @param version       版本号
     * @param merchant      商户号
     * @return 生成的 xml 数据
     */
    public String genReqXml(String rsaPrivateKey, String strDesKey, String version, String merchant) {

        if (StringKit.isEmpty(version) || StringKit.isEmpty(merchant)) {
            throw new RuntimeException("version or merchant is empty");
        }
        String encrypt = JdPayKit.encrypt(rsaPrivateKey, strDesKey, JdPayKit.toJdXml(toMap()));
        Map<String, String> requestMap = JdRequestModel.builder()
                .version(version)
                .merchant(merchant)
                .encrypt(encrypt)
                .build()
                .toMap();
        return JdPayKit.toJdXml(requestMap);
    }

    /**
     * PC H5 支付创建签名
     *
     * @param rsaPrivateKey RSA 私钥
     * @param strDesKey     DES 密钥
     * @return 生成签名后的 Map
     */
    public Map<String, String> createSign(String rsaPrivateKey, String strDesKey) {
        Map<String, String> map = toMap();
        // 生成签名
        String sign = JdPayKit.signRemoveSelectedKeys(map, rsaPrivateKey, new ArrayList<>());
        map.put("sign", sign);
        // 3DES进行加密
        return JdPayKit.threeDesToMap(map, strDesKey);
    }

}
