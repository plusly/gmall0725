package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manager.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.BaseAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BaseAttrValueServiceImpl implements BaseAttrValueService {

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    public void save(String id, List<BaseAttrValue> attrValueList){

        for (BaseAttrValue baseAttrValue : attrValueList){
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }

    }

    @Override
    public List<BaseAttrValue> getAttrValue(String attrInfoId) {

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrInfoId);

        return baseAttrValueMapper.select(baseAttrValue);
    }
}
