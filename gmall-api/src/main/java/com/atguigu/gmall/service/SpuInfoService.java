package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SpuInfo;

import java.util.List;

public interface SpuInfoService {

    List<SpuInfo> getAll(String ctg3Id);

    void saveSpu(SpuInfo spuInfo);
}
