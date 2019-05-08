package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.manager.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.service.SpuSaleAttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuSaleAttrServiceImpl implements SpuSaleAttrService {

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Override
    public List<SpuSaleAttr> getSpuSaleAttr(String spuId) {

        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);

        return spuSaleAttrMapper.select(spuSaleAttr);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId, String skuId) {

        return spuSaleAttrMapper.selectSpuSaleAttrListBySpuId(spuId, skuId);
    }
}
