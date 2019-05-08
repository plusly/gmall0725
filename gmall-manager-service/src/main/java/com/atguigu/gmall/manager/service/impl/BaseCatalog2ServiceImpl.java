package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.manager.mapper.BaseCatalog2Mapper;
import com.atguigu.gmall.service.BaseCatalog2Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BaseCatalog2ServiceImpl implements BaseCatalog2Service {

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    public List<BaseCatalog2> getAll(String ctg1Id) {

        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(ctg1Id);

        return baseCatalog2Mapper.select(baseCatalog2);

    }
}
