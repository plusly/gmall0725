package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;

import java.util.List;

public interface SkuInfoService {
    List<SkuInfo> getSkuInfoListBySpuId(String spuId);

    void saveSku(SkuInfo skuInfo);

    SkuInfo getSkuInfoBySkuId(String skuId);

    List<SkuInfo> skuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> getMySkuInfo(String catalog3Id);
}
