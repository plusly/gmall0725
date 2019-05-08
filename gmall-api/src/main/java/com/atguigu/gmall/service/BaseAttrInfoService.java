package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;

import java.util.HashSet;
import java.util.List;

public interface BaseAttrInfoService {
    List<BaseAttrInfo> getAttrList(String ctg3Id);

    String save(BaseAttrInfo baseAttrInfo);

    List<BaseAttrInfo> getAttrListByValueIds(HashSet<String> valueIds);
}
