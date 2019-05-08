package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.PaymentWay;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserAddressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class OrderHandler {

    @Reference
    UserAddressService userAddressService;

    @Reference
    OrderService orderService;

    @Reference
    CartInfoService cartInfoService;

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, OrderInfo orderInfo,String tradeCode, String addressId, ModelMap map){
        String userId = (String)request.getAttribute("userId");
        String nickName = (String)request.getAttribute("nickName");

        //防止表单重复提交，验证tradeCode
        boolean b = orderService.checkTradeCode(userId, tradeCode);
        if(b){
            // 生成订单和订单详情数据,db
            OrderInfo orderInfoDB = new OrderInfo();
            //获取用户地址信息
            UserAddress userAddress = userAddressService.getListByID(addressId);
            orderInfoDB.setConsignee(userAddress.getConsignee());
            orderInfoDB.setConsigneeTel(userAddress.getPhoneNum());
            orderInfoDB.setDeliveryAddress(userAddress.getUserAddress());
            //获取选中的购物车信息
            List<OrderDetail> orderDetailList = cartInfoService.getOrderDetailList(userId);
            orderInfoDB.setTotalAmount(getTotalPrice(orderDetailList));
            orderInfoDB.setOrderStatus(OrderStatus.UNPAID.toString());
            orderInfoDB.setProcessStatus("进度状态");
            orderInfoDB.setPaymentWay(PaymentWay.ONLINE);//支付方式
            orderInfoDB.setUserId(userId);
            orderInfoDB.setOrderComment(orderInfo.getOrderComment());

            //获取唯一订单编号
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String date = format.format(new Date());
            orderInfoDB.setOutTradeNo("stguigugmall"+System.currentTimeMillis()+date);
            orderInfoDB.setCreateTime(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            orderInfoDB.setExpireTime(calendar.getTime());//设置过期时间

            orderInfoDB.setOrderDetailList(orderDetailList);

            orderService.save(orderInfoDB);
            // 删除购物车数据
            //cartInfoService.deleteByOrder(userId, orderDetailList);
            // 重定向到支付页面
            return "redirect:http://payment.gmall.com:7070/payPage?outTradeNo="+orderInfoDB.getOutTradeNo()
                    +"&totalAmount="+orderInfoDB.getTotalAmount();
        }else{
            return "tradeFail";
        }

    }

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map){
        String userId = (String)request.getAttribute("userId");
        String nickName = (String)request.getAttribute("nickName");

        List<UserAddress> userAddressList = userAddressService.getList(userId);

        List<OrderDetail> orderDetailList = cartInfoService.getOrderDetailList(userId);

        boolean b = orderService.checkPrice(orderDetailList);

        if(b){
            map.put("nickName", nickName);
            map.put("orderDetailList", orderDetailList);
            map.put("userAddressList", userAddressList);
            map.put("totalAmount", getTotalPrice(orderDetailList));
            String tradeCode = UUID.randomUUID().toString();
            orderService.putTradeCodeToRedis(userId, tradeCode);
            map.put("tradeCode", tradeCode);
        }else{
            orderDetailList = orderService.alertPrice(orderDetailList);

            map.put("emg", "价格有变动");
            map.put("nickName", nickName);
            map.put("orderDetailList", orderDetailList);
            map.put("userAddressList", userAddressList);
            map.put("totalAmount", getTotalPrice(orderDetailList));
            String tradeCode = UUID.randomUUID().toString();
            orderService.putTradeCodeToRedis(userId, tradeCode);
            map.put("tradeCode", tradeCode);
        }
        //库存锁定消息
        orderService.lockRepertory(orderDetailList);

        return "trade";
    }

    private BigDecimal getTotalPrice(List<OrderDetail> orderDetailList) {
        BigDecimal sum = new BigDecimal("0");
        if(orderDetailList != null && orderDetailList.size() > 0){
            for (OrderDetail orderDetail : orderDetailList) {
                sum = sum.add(orderDetail.getOrderPrice());
            }
        }

        return sum;
    }
}
