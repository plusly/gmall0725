package com.atguigu.gmall.user.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.bean.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserHandler {

    @Reference
    UserInfoService userInfoService;

    @RequestMapping("/index")
    @ResponseBody
    public String index() {

        System.out.print("1111111");

        return "index";
    }

    @RequestMapping("/list/user")
    @ResponseBody
    public List<UserInfo> listUser() {

        List<UserInfo> list = userInfoService.userList();

        return list;
    }
}
