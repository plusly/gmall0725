package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> userList(){

        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo getUserInfoByLoginAndPasswd(UserInfo userInfo) {
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfo);

        if (userInfo1 != null) {
            //把userInfo1放入Redis
            Jedis jedis = redisUtil.getJedis();

            jedis.set("user:" + userInfo1.getId() + ":info", JSON.toJSONString(userInfo1));

            jedis.close();
        }
        return userInfo1;
    }
}
