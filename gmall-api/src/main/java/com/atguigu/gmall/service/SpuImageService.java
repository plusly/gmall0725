package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SpuImage;

import java.util.List;

public interface SpuImageService {
    List<SpuImage> getList(String spuId);
}
