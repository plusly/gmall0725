package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;

import java.util.ArrayList;
import java.util.List;

public interface CartInfoService {

    CartInfo selectCartExists(CartInfo cartInfo);

    void save(CartInfo cartInfo);

    void updata(CartInfo cartInfoDB);

    void flushCache(String userId);

    List<CartInfo> getCartListFromCache(String userId);

    void updataIsChecked(String skuId, String isChecked);

    List<CartInfo> getCartListByUserId(String userId);

    List<OrderDetail> getOrderDetailList(String userId);

    void deleteByOrder(String userId, List<OrderDetail> orderDetailList);
}
