package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo selectCartExists(CartInfo cartInfo) {

        CartInfo cartInfo1 = new CartInfo();
        cartInfo1.setUserId(cartInfo.getUserId());
        cartInfo1.setSkuId(cartInfo.getSkuId());

        CartInfo cartInfo2 = cartInfoMapper.selectOne(cartInfo);

        return cartInfo2;
    }

    @Override
    public void save(CartInfo cartInfo) {
        cartInfoMapper.insert(cartInfo);
    }

    @Override
    public void updata(CartInfo cartInfoDB) {

        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",cartInfoDB.getUserId())
                .andEqualTo("skuId",cartInfoDB.getSkuId());

        cartInfoMapper.updateByExampleSelective(cartInfoDB, example);
    }

    @Override
    public void flushCache(String userId) {
        Jedis jedis = redisUtil.getJedis();

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);

        jedis.del("user:" + userId + ":cart");

        Map<String, String> map = new HashMap<>();
        for (CartInfo info : cartInfos) {
            map.put(info.getSkuId(), JSON.toJSONString(info));
        }
        jedis.hmset("user:" + userId + ":cart", map);

        jedis.close();
    }

    @Override
    public List<CartInfo> getCartListFromCache(String userId) {
        Jedis jedis = redisUtil.getJedis();

        List<String> list = jedis.hvals("user:" + userId + ":cart");

        ArrayList<CartInfo> cartInfos = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (String s : list) {
                CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
                cartInfos.add(cartInfo);
            }
        }

        return cartInfos;
    }

    @Override
    public void updataIsChecked(String skuId, String isChecked) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("skuId", skuId).andEqualTo("isChecked", isChecked);

        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        cartInfo.setSkuId(skuId);

        cartInfoMapper.updateByExampleSelective(cartInfo,example);
    }

    @Override
    public List<CartInfo> getCartListByUserId(String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);

        return cartInfoMapper.select(cartInfo);
    }

    /**
     * 查出选中的购物车列表，创建OrderDetail集合并返回
     * @param userId
     * @return
     */
    @Override
    public List<OrderDetail> getOrderDetailList(String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);

        List<CartInfo> cartInfoList = cartInfoMapper.select(cartInfo);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        if(cartInfoList != null && cartInfoList.size() > 0){
            for (CartInfo info : cartInfoList) {
                if(info.getIsChecked().equals("1")){
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setImgUrl(info.getImgUrl());
                    orderDetail.setOrderPrice(info.getCartPrice());
                    orderDetail.setSkuId(info.getSkuId());
                    orderDetail.setSkuName(info.getSkuName());
                    orderDetail.setSkuNum(info.getSkuNum());
                    orderDetailList.add(orderDetail);
                }
            }
        }

        return orderDetailList;
    }

    @Override
    public void deleteByOrder(String userId, List<OrderDetail> orderDetailList) {
        if(orderDetailList != null && orderDetailList.size() > 0){

            for (OrderDetail orderDetail : orderDetailList) {
                String skuId = orderDetail.getSkuId();
                CartInfo cartInfo = new CartInfo();
                cartInfo.setUserId(userId);
                cartInfo.setSkuId(skuId);

                cartInfoMapper.delete(cartInfo);
            }
            flushCache(userId);
        }
    }
}
