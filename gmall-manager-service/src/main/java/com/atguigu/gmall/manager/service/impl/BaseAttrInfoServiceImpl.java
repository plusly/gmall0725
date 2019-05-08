package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.manager.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.service.BaseAttrInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(HashSet<String> valueIds) {

        String valueIdStr = StringUtils.join(valueIds,",");//1,2,3,4,67,8,56
        Map<String,String> map = new HashMap<>();
        map.put("valueIdStr",valueIdStr);
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectAttrListByValueIds(map);

        return baseAttrInfos;
    }

    public List<BaseAttrInfo> getAttrList(String ctg3Id){

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();

        baseAttrInfo.setCatalog3Id(ctg3Id);

        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    public String save(BaseAttrInfo baseAttrInfo){

        baseAttrInfoMapper.insertSelective(baseAttrInfo);
        String id = baseAttrInfo.getId();

        return id;
    }
}
