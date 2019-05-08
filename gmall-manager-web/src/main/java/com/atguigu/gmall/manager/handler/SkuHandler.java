package com.atguigu.gmall.manager.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SkuHandler {

    @Reference
    SkuInfoService skuInfoService;

    @RequestMapping("saveSku")
    public String saveSku(SkuInfo skuInfo){

        skuInfoService.saveSku(skuInfo);

        return "SUCCESS";
    }

    @RequestMapping("getSkuInfoList")
    public List<SkuInfo> getSkuInfoList(String spuId){

        return skuInfoService.getSkuInfoListBySpuId(spuId);
    }
}
