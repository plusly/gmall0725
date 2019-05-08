package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.bean.SpuSaleAttrValue;
import com.atguigu.gmall.manager.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.service.SpuSaleAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuSaleAttrValueServiceImpl implements SpuSaleAttrValueService {

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public List<SpuSaleAttrValue> getList(String spuId, String saleAttrId) {

        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuId);
        spuSaleAttrValue.setSaleAttrId(saleAttrId);

        return spuSaleAttrValueMapper.select(spuSaleAttrValue);
    }
}
