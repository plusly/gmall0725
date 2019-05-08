package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentMapper paymentMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public PaymentInfo getOneByOutTradeNo(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);

        return paymentMapper.selectOne(paymentInfo);
    }

    @Override
    public void sendDelayPaymentResult(PaymentInfo paymentInfo, int count){
        // 发送延迟支付的消息队列
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", paymentInfo.getOutTradeNo());
            mapMessage.setInt("count",count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> checkAlipayPayment(String out_trade_no) {

        HashMap<String, String> returnMap = new HashMap<>();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, String> map = new HashMap<>();
        map.put("out_trade_no", out_trade_no);

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.err.println("调用成功");
            String tradeNo = response.getTradeNo();
            String tradeStatus = response.getTradeStatus();
            String queryString = response.toString();
            returnMap.put("tradeNo", tradeNo);
            returnMap.put("tradeStatus", tradeStatus);
            returnMap.put("queryString", queryString);
        }else{
            System.err.println("调用失败");
        }
        return returnMap;
    }

    @Override
    public void sendPaymentSuccessQueue(PaymentInfo paymentInfo) {
        // 发送支付成功的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOutTradeNo());
            mapMessage.setString("trade_no", paymentInfo.getAlipayTradeNo());
            mapMessage.setString("result", paymentInfo.getPaymentStatus());

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkPayStatus(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfo1 = paymentMapper.selectOne(paymentInfo);

        if(paymentInfo1 != null && "已支付".equals(paymentInfo1.getPaymentStatus())){
            return true;
        }

        return false;
    }

    @Override
    public void update(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", paymentInfo.getOutTradeNo());
        paymentMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void save(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }
}
