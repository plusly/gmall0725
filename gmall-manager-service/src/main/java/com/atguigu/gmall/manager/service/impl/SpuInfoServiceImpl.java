package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.bean.SpuSaleAttrValue;
import com.atguigu.gmall.manager.mapper.SpuImageMapper;
import com.atguigu.gmall.manager.mapper.SpuInfoMapper;
import com.atguigu.gmall.manager.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.manager.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Override
    public void saveSpu(SpuInfo spuInfo) {

        spuInfoMapper.insertSelective(spuInfo);
        String spuId = spuInfo.getId();

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuId);
            spuImageMapper.insertSelective(spuImage);
        }

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for(SpuSaleAttr spuSaleAttr : spuSaleAttrList){
            spuSaleAttr.setSpuId(spuId);
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for(SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList){
                spuSaleAttrValue.setSpuId(spuId);
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }

    }

    @Override
    public List<SpuInfo> getAll(String ctg3Id) {

        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(ctg3Id);

        return spuInfoMapper.select(spuInfo);
    }
}
