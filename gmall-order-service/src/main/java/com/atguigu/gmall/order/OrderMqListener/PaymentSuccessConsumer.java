package com.atguigu.gmall.order.OrderMqListener;

import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentSuccessConsumer {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        String trade_no = mapMessage.getString("trade_no");
        String result = mapMessage.getString("result");

        orderService.updateByOutTradeNo(out_trade_no,trade_no,result);

        orderService.sendOrderResult(out_trade_no);

    }
}
