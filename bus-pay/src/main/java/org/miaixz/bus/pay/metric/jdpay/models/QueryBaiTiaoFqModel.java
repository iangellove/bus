package org.miaixz.bus.pay.metric.jdpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class QueryBaiTiaoFqModel extends JdPayEntity {

    private String version;
    private String merchant;
    private String tradeNum;
    private String amount;
    private String sign;

}
