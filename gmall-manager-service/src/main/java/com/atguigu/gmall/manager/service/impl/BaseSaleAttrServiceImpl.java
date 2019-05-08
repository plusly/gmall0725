package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.manager.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.service.BaseSaleAttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BaseSaleAttrServiceImpl implements BaseSaleAttrService {

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Override
    public List<BaseSaleAttr> getAll() {

        return baseSaleAttrMapper.selectAll();
    }
}
