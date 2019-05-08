package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.mapper.OrderdetailMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderdetailMapper orderdetailMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    SkuInfoService skuInfoService;

    @Override
    public void putTradeCodeToRedis(String userId, String tradeCode) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:"+ userId + ":tradeCode", 60 * 60, tradeCode);

        jedis.close();
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean b = false;

        Jedis jedis = redisUtil.getJedis();
        String code = jedis.get("user:" + userId + ":tradeCode");

        if(StringUtils.isNotBlank(code)){
            if(tradeCode.equals(code)){
                jedis.del("user:" + userId + ":tradeCode");
                b = true;
            }
        }
        jedis.close();
        return b;
    }

    @Override
    public void save(OrderInfo orderInfoDB) {
        orderInfoMapper.insertSelective(orderInfoDB);
        String orderId = orderInfoDB.getId();

        List<OrderDetail> orderDetailList = orderInfoDB.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderdetailMapper.insertSelective(orderDetail);
        }
    }

    @Override
    public OrderInfo getOneByOutTradeNo(String outTradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);

        OrderInfo orderInfo1 = orderInfoMapper.selectOne(orderInfo);
        String orderId = orderInfo1.getId();

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderdetailMapper.select(orderDetail);

        orderInfo1.setOrderDetailList(orderDetails);

        return orderInfo1;
    }

    @Override
    public void updateByOutTradeNo(String out_trade_no,String trade_no,String result) {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTrackingNo(trade_no);
        orderInfo.setOrderStatus(result);

        orderInfoMapper.updateByExampleSelective(orderInfo, example);
    }

    @Override
    public void sendOrderResult(String out_trade_no) {
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("ORDER_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            OrderInfo orderInfo = getOneByOutTradeNo(out_trade_no);
            String order = JSON.toJSONString(orderInfo);
            textMessage.setText(order);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验价
     * @param orderDetailList
     * @return
     */
    @Override
    public boolean checkPrice(List<OrderDetail> orderDetailList) {
        ArrayList<Integer> list = new ArrayList<>();

        for (OrderDetail orderDetail : orderDetailList) {
            String skuId = orderDetail.getSkuId();
            Integer skuNum = orderDetail.getSkuNum();
            BigDecimal orderPrice = orderDetail.getOrderPrice();

            SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
            int i = orderPrice.compareTo(skuInfo.getPrice().multiply(new BigDecimal(skuNum)));

            list.add(i);
        }

        if(list.contains(-1) || list.contains(1)){
            return false;
        }

        return true;
    }

    @Override
    public List<OrderDetail> alertPrice(List<OrderDetail> orderDetailList) {
        for (OrderDetail orderDetail : orderDetailList) {
            String skuId = orderDetail.getSkuId();
            Integer skuNum = orderDetail.getSkuNum();

            SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);

            orderDetail.setOrderPrice(skuInfo.getPrice().multiply(new BigDecimal(skuNum)));
        }

            return orderDetailList;
    }

    @Override
    public void lockRepertory(List<OrderDetail> orderDetailList) {
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("LOCK_REPERTORY_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            String s = JSON.toJSONString(orderDetailList);
            mapMessage.setString("orderDetailList",s);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
