package com.atguigu.gmall.payment.paymentMqListener;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PayCheckStatusListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {
        int count = mapMessage.getInt("count");
        String out_trade_no = mapMessage.getString("out_trade_no");
        // 检查支付状态
        System.err.println("开始检查支付状态：第"+(6-count)+"次");
        Map<String, String> success = paymentService.checkAlipayPayment(out_trade_no);
        String tradeStatus = success.get("tradeStatus");
        String tradeNo = success.get("tradeNo");
        String queryString = success.get("queryString");

        count--;

        if(tradeStatus != null && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))){
            //幂等性检查
            boolean b = paymentService.checkPayStatus(out_trade_no);
            if(!b){
                //支付成功，更新支付信息
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setAlipayTradeNo(tradeNo);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setOutTradeNo(out_trade_no);
                paymentInfo.setCallbackContent(queryString);
                paymentService.update(paymentInfo);

                //通知订单系统，修改订单状态
                paymentService.sendPaymentSuccessQueue(paymentInfo);
            }else{
                System.out.println("支付状态已更新，不再重复更新");
            }

        }else {
            if (count > 0) {
                System.err.println("未支付成功，继续发送下一次的延迟检查任务，剩余次数："+count);
                PaymentInfo paymentInfo = paymentService.getOneByOutTradeNo(out_trade_no);
                paymentService.sendDelayPaymentResult(paymentInfo, count);
            }else{
                System.err.println("次数用尽，停止延迟检查");
            }
        }

    }
}
