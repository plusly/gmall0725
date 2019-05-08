package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.BaseAttrInfoService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import sun.security.util.Length;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    BaseAttrInfoService baseAttrInfoService;

    @RequestMapping("index.html")
    public String toIndexPage(){
        return "index";
    }

    @RequestMapping("list.html")
    public String toListPage(SkuLsParam skuLsParam, ModelMap map){
        //查询到的商品信息
        List<SkuLsInfo> skuLsInfos = listService.search(skuLsParam);
        map.put("skuLsInfoList", skuLsInfos);

        //获取skuLsInfos中的valueId，放入一个set集合中
        HashSet<String> valueIds = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfos) {

            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();

            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {

                valueIds.add(skuLsAttrValue.getValueId());
            }
        }
        //根据valueIds获取平台属性的对象
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoService.getAttrListByValueIds(valueIds);
        map.put("attrList", baseAttrInfos);

        //删除已选valueID的那行平台属性
        String[] valueIdArr = skuLsParam.getValueId();
        if (valueIdArr != null && valueIdArr.length > 0) {
            ArrayList<Crumb> attrValueSelectedList = new ArrayList<>();//制作面包屑
            for (String delValueId : valueIdArr) {
                Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
                while (iterator.hasNext()) {
                    BaseAttrInfo baseAttrInfo = iterator.next();
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                        if(delValueId.equals(baseAttrValue.getId())){
                            Crumb crumb = new Crumb();//制作面包屑
                            String crumbUrl = getMyUrlParam(skuLsParam, delValueId);//制作面包屑
                            crumb.setUrlParam(crumbUrl);//制作面包屑
                            crumb.setValueName(baseAttrValue.getValueName());//制作面包屑
                            attrValueSelectedList.add(crumb);//制作面包屑
                            iterator.remove();//移除平台属性
                        }
                    }
                }
            }
            map.put("attrValueSelectedList", attrValueSelectedList);
        }


        //定义URLParam
        String urlParam = getMyUrlParam(skuLsParam, "");
        map.put("urlParam", urlParam);
        //把关键字传入list.html
        map.put("keyword", skuLsParam.getKeyword());

        //制作面包屑
//        if (valueIdArr != null && valueIdArr.length > 0) {
//            ArrayList<Crumb> attrValueSelectedList = new ArrayList<>();
//            for (String crumbValueId : valueIdArr) {
//                Crumb crumb = new Crumb();
//                String crumbUrl = getMyUrlParam(skuLsParam, crumbValueId);
//                crumb.setUrlParam(crumbUrl);
//                String valueName = "";
//                for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
//                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//                    for (BaseAttrValue baseAttrValue : attrValueList) {
//                        if(crumbValueId.equals(baseAttrValue.getId())){
//                            valueName = baseAttrValue.getValueName();
//                            break;
//                        }
//                    }
//                }
//                crumb.setValueName(valueName);
//                attrValueSelectedList.add(crumb);
//            }
//            map.put("attrValueSelectedList", attrValueSelectedList);
//        }

        return "list";
    }

    private String getMyUrlParam(SkuLsParam skuLsParam, String delValueId) {

        String urlParam = "";

        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueIds = skuLsParam.getValueId();

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }

            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }

            urlParam = urlParam + "keyword=" + keyword;
        }

        if (valueIds != null) {

            for (String valueId : valueIds) {
                if(!valueId.equals(delValueId)){
                    urlParam = urlParam + "&valueId=" + valueId;
                }
            }
        }

        return urlParam;
    }


}
