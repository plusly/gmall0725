package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void save(PaymentInfo paymentInfo);

    void update(PaymentInfo paymentInfo);

    boolean checkPayStatus(String out_trade_no);

    Map<String,String> checkAlipayPayment(String out_trade_no);

    void sendPaymentSuccessQueue(PaymentInfo paymentInfo);

    void sendDelayPaymentResult(PaymentInfo paymentInfo, int count);

    PaymentInfo getOneByOutTradeNo(String out_trade_no);
}
