package com.atguigu.gmall.manager.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.service.BaseAttrInfoService;
import com.atguigu.gmall.service.BaseAttrValueService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BaseAttrHandler {

    @Reference
    BaseAttrInfoService baseAttrInfoService;

    @Reference
    BaseAttrValueService baseAttrValueService;

    @RequestMapping("getAttrList")
    public List<BaseAttrInfo> getAttrList(String ctg3Id){
        List<BaseAttrInfo> attrList = baseAttrInfoService.getAttrList(ctg3Id);
        return attrList;
    }

    @RequestMapping("saveAttr")
    public String saveAttr(BaseAttrInfo baseAttrInfo){

        String id = baseAttrInfoService.save(baseAttrInfo);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        baseAttrValueService.save(id, attrValueList);

        return "SUCCESS";
    }

    @RequestMapping("getAttrValue")
    public List<BaseAttrValue> getAttrValue(String attrInfoId){

        return baseAttrValueService.getAttrValue(attrInfoId);
    }

    @RequestMapping("aaaa")
    public String aaaa(String id){
        System.out.print(id);
        return "aaa";
    }
}
