package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.manager.mapper.BaseCatalog1Mapper;
import com.atguigu.gmall.service.BaseCatalog1Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BaseCatalog1ServiceImpl implements BaseCatalog1Service {

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    public List<BaseCatalog1> getAll(){

        return baseCatalog1Mapper.selectAll();
    }
}
