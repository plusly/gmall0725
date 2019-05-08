package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SpuSaleAttrService {
    List<SpuSaleAttr> getSpuSaleAttr(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId, String skuId);
}
