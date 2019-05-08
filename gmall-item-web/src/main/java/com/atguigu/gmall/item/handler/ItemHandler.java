package com.atguigu.gmall.item.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SpuSaleAttrService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemHandler {

    @Reference
    SkuInfoService skuInfoService;

    @Reference
    SpuSaleAttrService spuSaleAttrService;

    @RequestMapping("{skuId}.html")
    public String toItemPage(@PathVariable String skuId, ModelMap modelMap){

        SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
        modelMap.put("skuInfo", skuInfo);

        String spuId = skuInfo.getSpuId();

        HashMap<String, String> stringStringHashMap = new HashMap<>();
        //得到封装好skuSaleAttrValueList的skuInfo对象
        List<SkuInfo> skuInfos =  skuInfoService.skuSaleAttrValueListBySpu(spuId);
        for (SkuInfo info : skuInfos) {
            String skuSaleAttrValueIdsKey = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrService.getSpuSaleAttrListBySpuId(spuId,skuId);
            modelMap.put("spuSaleAttrListCheckBySku", spuSaleAttrList);
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //获取SaleAttrValueId作为key
                skuSaleAttrValueIdsKey = skuSaleAttrValueIdsKey + "|" + skuSaleAttrValue.getSaleAttrValueId();
            }
            //获取skuId作为value
            String infoId = info.getId();
            stringStringHashMap.put(skuSaleAttrValueIdsKey,infoId);
        }

        String s = JSON.toJSONString(stringStringHashMap);
        modelMap.put("valuesSkuJson",s);

        return "item";
    }


}
