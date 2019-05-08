package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrValue;

import java.util.List;

public interface BaseAttrValueService {
    void save(String id, List<BaseAttrValue> attrValueList);

    List<BaseAttrValue> getAttrValue(String attrInfoId);
}
