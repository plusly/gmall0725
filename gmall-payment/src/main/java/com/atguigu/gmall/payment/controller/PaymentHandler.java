package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.utils.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentHandler {
    //只需在创建DefaultAlipayClient对象时，设置请求网关(gateway)，应用id(app_id)，应用私钥(private_key)，
    // 编码格式(charset)，支付宝公钥(alipay_public_key)，签名类型(sign_type)即可，报文请求时会自动进行签名。
    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @RequestMapping("payPage")
    public String paymentIndex(String outTradeNo, BigDecimal totalAmount, ModelMap map){
        map.put("totalAmount", totalAmount);
        map.put("outTradeNo", outTradeNo);
        return "index";
    }

    @RequestMapping("alipay/submit")
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount){

        OrderInfo orderInfo = orderService.getOneByOutTradeNo(outTradeNo);
        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentService.save(paymentInfo);

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        HashMap<String, String> aliMap = new HashMap<>();
        aliMap.put("out_trade_no",outTradeNo);
        aliMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        //aliMap.put("total_amount",totalAmount.toString());
        aliMap.put("total_amount","0.01");
        aliMap.put("subject","订单标题");

        alipayRequest.setBizContent(JSON.toJSONString(aliMap));//填充业务参数

        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        paymentService.sendDelayPaymentResult(paymentInfo,5);

        return form;
    }

    @RequestMapping("alipay/callback/return")
    public String returnPaymen(HttpServletRequest request){
        //付款成功返回的参数
        String trade_no = request.getParameter("trade_no");//付款编号
        String out_trade_no = request.getParameter("out_trade_no");//订单交易编号
        String total_amount = request.getParameter("total_amount");//总金额
        String sign = request.getParameter("sign");//签名
        String app_id = request.getParameter("app_id");//支付宝分配给开发者的应用Id
        String notify_time = request.getParameter("notify_time");//通知的发送时间
        String trade_status = request.getParameter("trade_status");//支付状态
        //判断是否已支付
        boolean b = paymentService.checkPayStatus(out_trade_no);
        if(!b){
            Map<String, String> map = new HashMap<>();
            try {
                //调用SDK验证签名
                boolean signVerified = AlipaySignature.rsaCheckV1(map, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }

            // 更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentService.update(paymentInfo);

            //通知订单系统，修改订单状态
            paymentService.sendPaymentSuccessQueue(paymentInfo);

            return "finish";
        }else{
            return "past";
        }
    }
}
