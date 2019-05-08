package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;

import java.util.List;

public interface OrderService {
    void putTradeCodeToRedis(String userId,String tradeCode);

    boolean checkTradeCode(String userId, String tradeCode);

    void save(OrderInfo orderInfoDB);

    OrderInfo getOneByOutTradeNo(String outTradeNo);

    void updateByOutTradeNo(String out_trade_no,String trade_no,String result);

    void sendOrderResult(String out_trade_no);

    boolean checkPrice(List<OrderDetail> orderDetailList);

    List<OrderDetail> alertPrice(List<OrderDetail> orderDetailList);

    void lockRepertory(List<OrderDetail> orderDetailList);
}
