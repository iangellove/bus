package org.miaixz.bus.pay.metric.jdpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FkmModel extends JdPayEntity {

    private String token;
    private String version;
    private String merchant;
    private String device;
    private String tradeNum;
    private String tradeName;
    private String tradeDesc;
    private String tradeTime;
    private String amount;
    private String industryCategoryCode;
    private String currency;
    private String note;
    private String notifyUrl;
    private String orderGoodsNum;
    private String vendorId;
    private String goodsInfoList;
    private String receiverInfo;
    private String termInfo;
    private String payMerchant;
    private String sign;
    private String riskInfo;
    private String bizTp;

}
