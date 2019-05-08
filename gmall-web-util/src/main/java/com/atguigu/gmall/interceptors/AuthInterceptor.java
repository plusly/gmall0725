package com.atguigu.gmall.interceptors;

import com.atguigu.gmall.annotations.LoginRequire;
import com.atguigu.gmall.utils.CookieUtil;
import com.atguigu.gmall.utils.HttpClientUtil;
import com.atguigu.gmall.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import sun.font.TrueTypeFont;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HandlerMethod hm = (HandlerMethod) handler;

        LoginRequire loginRequire = hm.getMethodAnnotation(LoginRequire.class);

        if (loginRequire == null) {
            return true;
        }

        boolean neededSuccess = loginRequire.isNeededSuccess();

        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "userToken", true);
        String newToken = request.getParameter("userToken");

        if (oldToken != null) {
            token = oldToken;
        }
        if (newToken != null) {
            token = newToken;
        }

        if(StringUtils.isNotBlank(token)){
            // 验证token，http的工具
            // 通过负载均衡nginx
            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
            }
            //验证token
            String doGet = HttpClientUtil.doGet("http://passport.gmall.com:8000/verify?token=" + token + "&currentIp=" + ip);

            if("success".equals(doGet)){
                //把token放入cookie中
                CookieUtil.setCookie(request,response,"userToken",token,60*60,true );
                //将用户的getRemoteAddr昵称与ID写入
                Map gmall0725 = JwtUtil.decode("gmall0725", token, ip);
                request.setAttribute("userId", gmall0725.get("userId"));
                request.setAttribute("nickName",gmall0725.get("nickName"));

                return true;
            }
        }

        // token为空或者验证不通过
        if(neededSuccess == true){
            String originUrl = request.getRequestURL().toString();
            response.sendRedirect("http://passport.gmall.com:8000/login?originUrl="+originUrl );
            return false;
        }
        // token为空且不需要登陆
        return true;
    }
}
