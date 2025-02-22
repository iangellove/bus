package org.miaixz.bus.pay.metric.wxpay.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * V3 统一下单-支付者
 */
@Getter
@Setter
@Accessors(chain = true)
public class Payer {

    /**
     * 用户标识
     */
    private String openid;
    /**
     * 用户服务标识
     */
    private String sp_openid;
    /**
     * 用户子标识
     */
    private String sub_openid;

}
