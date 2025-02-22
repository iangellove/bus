package org.miaixz.bus.pay.metric.qqpay;

public class ApiUrl {

    /**
     * 提交付款码支付
     */
    public static final String MICRO_PAY_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_micro_pay.cgi";
    /**
     * 统一下单
     */
    public static final String UNIFIED_ORDER_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_unified_order.cgi";

    /**
     * 订单查询
     */
    public static final String ORDER_QUERY_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_order_query.cgi";

    /**
     * 关闭订单
     */
    public static final String CLOSE_ORDER_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_close_order.cgi";

    /**
     * 撤销订单
     */
    public static final String ORDER_REVERSE_URL = "https://api.qpay.qq.com/cgi-bin/pay/qpay_reverse.cgi";

    /**
     * 申请退款
     */
    public static final String ORDER_REFUND_URL = "https://api.qpay.qq.com/cgi-bin/pay/qpay_refund.cgi";

    /**
     * 退款查询
     */
    public static final String REFUND_QUERY_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_refund_query.cgi";

    /**
     * 对账单下载
     */
    public static final String DOWNLOAD_BILL_URL = "https://qpay.qq.com/cgi-bin/sp_download/qpay_mch_statement_down.cgi";

    /**
     * 创建现金红包
     */
    public static final String CREATE_READ_PACK_URL = "https://api.qpay.qq.com/cgi-bin/hongbao/qpay_hb_mch_send.cgi";
    /**
     * 查询红包详情
     */
    public static final String GET_HB_INFO_URL = "https://qpay.qq.com/cgi-bin/mch_query/qpay_hb_mch_list_query.cgi";
    /**
     * 红包对账单下载
     */
    public static final String DOWNLOAD_HB_BILL_URL = "https://api.qpay.qq.com/cgi-bin/hongbao/qpay_hb_mch_down_list_file.cgi";

    /**
     * 企业付款到余额
     */
    public static final String TRANSFER_URL = "https://api.qpay.qq.com/cgi-bin/epay/qpay_epay_b2c.cgi";
    /**
     * 查询企业付款
     */
    public static final String GET_TRANSFER_INFO_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_epay_query.cgi";
    /**
     * 企业付款对账单下载
     */
    public static final String DOWNLOAD_TRANSFER_BILL_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_epay_statement_down.cgi";

}
