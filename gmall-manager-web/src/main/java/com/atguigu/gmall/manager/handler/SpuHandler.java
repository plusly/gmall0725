package com.atguigu.gmall.manager.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.bean.SpuSaleAttrValue;
import com.atguigu.gmall.manager.Utils.GmallUploadUtil;
import com.atguigu.gmall.service.SpuImageService;
import com.atguigu.gmall.service.SpuInfoService;
import com.atguigu.gmall.service.SpuSaleAttrService;
import com.atguigu.gmall.service.SpuSaleAttrValueService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.util.List;

@RestController
public class SpuHandler {

    @Reference
    SpuInfoService spuInfoService;

    @Reference
    SpuSaleAttrService spuSaleAttrService;

    @Reference
    SpuSaleAttrValueService spuSaleAttrValueService;

    @Reference
    SpuImageService spuImageService;

    @RequestMapping("getSpuImageList")
    public List<SpuImage> getSpuImageList(String spuId){

        return spuImageService.getList(spuId);
    }

    @RequestMapping("getSpuSaleAttrValue")
    public List<SpuSaleAttrValue> getSpuSaleAttrValue(SpuSaleAttr param){

        return spuSaleAttrValueService.getList(param.getSpuId(), param.getSaleAttrId());
    }

    @RequestMapping("getSpuSaleAttr")
    public List<SpuSaleAttr> getSpuSaleAttr(String spuId){

        return spuSaleAttrService.getSpuSaleAttr(spuId);
    }

    @RequestMapping("getSpuList")
    public List<SpuInfo> getSpuList(String ctg3Id){

        return spuInfoService.getAll(ctg3Id);
    }

    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){

        String imgUrl = GmallUploadUtil.uploadImg(multipartFile);

        return imgUrl;
    }
}
