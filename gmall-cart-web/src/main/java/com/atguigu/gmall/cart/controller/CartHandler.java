package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequire;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartHandler {

    @Reference
    SkuInfoService skuInfoService;

    @Reference
    CartInfoService cartInfoService;

    @Reference
    UserInfoService userInfoService;

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(String skuId, String isChecked, ModelMap map, HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfos = new ArrayList<>();
        if(StringUtils.isNotBlank(userId)){
            //从db中取数据
            cartInfos = cartInfoService.getCartListByUserId(userId);
            for (CartInfo cartInfo : cartInfos) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                    cartInfoService.updata(cartInfo);
                    break;
                }
            }
            cartInfoService.flushCache(userId);
        }else {
            //从cookie中取数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
            for (CartInfo cartInfo : cartInfos) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                    break;
                }
            }
            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(cartInfos),60*60*24,true);
        }

        map.put("cartList", cartInfos);
        map.put("totalPrice", getTotalPrice(cartInfos));

        return "cartListInner";
    }

    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap map){

        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfos = new ArrayList<>();
        //判断是否登录
        if(StringUtils.isBlank(userId)){
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //从cookie中获取购物车列表
            if(StringUtils.isNotBlank(cartListCookie)){
                //将JSON字符串转换成cartInfo对象
                cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
            }
        }else{
            //从缓存中获取购物车列表
            cartInfos = cartInfoService.getCartListFromCache(userId);
        }

        map.put("cartList", cartInfos);
        //结算总金额
        BigDecimal totalPrice = getTotalPrice(cartInfos);
        map.put("totalPrice", totalPrice);

        return "cartList";
    }

    private BigDecimal getTotalPrice(List<CartInfo> cartInfos) {
        BigDecimal sum = new BigDecimal("0");
        if(cartInfos != null && cartInfos.size() > 0){
            for (CartInfo cartInfo : cartInfos) {
                if("1".equals(cartInfo.getIsChecked())){
                    sum = sum.add(cartInfo.getCartPrice());
                }
            }
        }

        return sum;
    }

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(String skuId, int num, HttpServletRequest request, HttpServletResponse response){

        SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
        String skuName = skuInfo.getSkuName();
        String skuDefaultImg = skuInfo.getSkuDefaultImg();

        String encode = null;
        try {
            encode = URLEncoder.encode(skuName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String userId = (String) request.getAttribute("userId");

        //封装cartInfo
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuNum(num);
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(num)));
        cartInfo.setImgUrl(skuDefaultImg);
        cartInfo.setSkuName(skuName);
        cartInfo.setIsChecked("1");
        cartInfo.setSkuPrice(skuInfo.getPrice());

        //创建购物车列表数组
        List<CartInfo> cartInfos = new ArrayList<>();
        if(StringUtils.isBlank(userId)){
            //获取cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //判断cookie是否为空
            if(StringUtils.isBlank(cartListCookie)){
                //向购物车列表添加一组数据
                cartInfos.add(cartInfo);
            }else {
                //将cookie转为cartInfo集合
                cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
                //判断cartInfo在数组中有没有
                boolean b = is_new_cart(cartInfos,cartInfo);

                if(b){
                    //向购物车列表添加一组数据
                    cartInfos.add(cartInfo);
                }else {
                    for (CartInfo info : cartInfos) {
                        if(info.getSkuId().equals(cartInfo.getSkuId())){
                            //更新原来的cartInfo
                            info.setSkuNum(info.getSkuNum()+cartInfo.getSkuNum());
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            }
            //覆盖原来的cookie
            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(cartInfos),60*60*24,true);
        }else {
            cartInfo.setUserId(userId);
            //根据userID和skuId查询数据库中是否有这个cartInfo存在
            CartInfo cartInfoDB = cartInfoService.selectCartExists(cartInfo);
            if (cartInfoDB != null) {
                cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfo.getSkuNum());
                cartInfoDB.setCartPrice(cartInfoDB.getSkuPrice().multiply(new BigDecimal(cartInfoDB.getSkuNum())));

                cartInfoService.updata(cartInfoDB);
            }else{
                cartInfoService.save(cartInfo);
            }
            // 同步购物车缓存
            cartInfoService.flushCache(userId);
        }

        return "redirect:/cartAddSuccess?id="+skuId+"&skuName="+encode+"&skuDefaultImg="+skuDefaultImg+"&skuNum="+num;
    }

    private boolean is_new_cart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo info : cartInfos) {
            if(info.getSkuId().equals(cartInfo.getSkuId())){
                b = false;
            }
        }
        return b;
    }

    @LoginRequire(isNeededSuccess = false)
    @RequestMapping("cartAddSuccess")
    public String toCartPage(SkuInfo skuInfo, int skuNum, Model model){

        String skuName = skuInfo.getSkuName();
        String decode = null;
        try {
            decode = URLDecoder.decode(skuName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        skuInfo.setSkuName(decode);

        model.addAttribute("skuInfo", skuInfo);
        model.addAttribute("skuNum", skuNum);

        return "success";
    }


}
