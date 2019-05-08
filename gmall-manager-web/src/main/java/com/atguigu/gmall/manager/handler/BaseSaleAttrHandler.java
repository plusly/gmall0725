package com.atguigu.gmall.manager.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.BaseSaleAttrService;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BaseSaleAttrHandler {

    @Reference
    BaseSaleAttrService baseSaleAttrService;

    @Reference
    SpuInfoService spuInfoService;

    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        List<BaseSaleAttr> list = baseSaleAttrService.getAll();

        return list;
    }

    @RequestMapping("saveSpu")
    public String saveSpu(SpuInfo spuInfo){

        spuInfoService.saveSpu(spuInfo);

        return "SUCCESS";
    }
}
