package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.utils.CookieUtil;
import com.atguigu.gmall.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PassportHandler {

    @Reference
    UserInfoService userInfoService;

    @Reference
    CartInfoService cartInfoService;

    @RequestMapping("login")
    public String toLoginPage(String originUrl, ModelMap map){

        map.put("originUrl", originUrl);

        return "login";
    }

    @RequestMapping("doLogin")
    @ResponseBody
    public String doLogin(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){

        //判断登录的用户名是否正确
        UserInfo userInfo1 = userInfoService.getUserInfoByLoginAndPasswd(userInfo);

        if (userInfo1 == null) {
            return "fail";
        }else{
            Map<String, String> map = new HashMap<>();

            map.put("userId", userInfo1.getId().toString());
            map.put("nickName", userInfo1.getNickName());

            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();
            }

            String token = JwtUtil.encode("gmall0725", map, ip);

            //合并cookie中的数据与用户数据库
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //从cookie中获取购物车列表
            if(StringUtils.isNotBlank(cartListCookie)){
                //将JSON字符串转换成cartInfo对象
                List<CartInfo> cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
                //把cookie中的数据同步到用户数据库
                for (CartInfo cartInfo : cartInfos) {
                    cartInfo.setUserId(userInfo1.getId().toString());
                    CartInfo cartInfoDB = cartInfoService.selectCartExists(cartInfo);
                    if (cartInfoDB != null) {
                        cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfo.getSkuNum());
                        cartInfoDB.setCartPrice(cartInfoDB.getSkuPrice().multiply(new BigDecimal(cartInfoDB.getSkuNum())));

                        cartInfoService.updata(cartInfoDB);
                    }else{
                        cartInfoService.save(cartInfo);
                    }
                }
                //删除浏览器中的cookie
                CookieUtil.deleteCookie(request, response,"cartListCookie");
                // 同步购物车缓存
                cartInfoService.flushCache(userInfo1.getId().toString());
            }

            return token;
        }
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp){
        Map gmall0725 = JwtUtil.decode("gmall0725", token, currentIp);

        if (gmall0725 == null) {
            return "fail";
        }else{
            return "success";
        }
    }
}
