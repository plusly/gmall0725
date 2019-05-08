package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.bean.SpuSaleAttrValue;

import javax.swing.*;
import java.util.List;

public interface SpuSaleAttrValueService {
    List<SpuSaleAttrValue> getList(String spuId, String saleAttrId);
}
