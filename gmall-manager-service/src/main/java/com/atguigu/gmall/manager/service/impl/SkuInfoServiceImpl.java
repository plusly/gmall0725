package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuAttrValue;
import com.atguigu.gmall.bean.SkuImage;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.manager.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.SkuImageMapper;
import com.atguigu.gmall.manager.mapper.SkuInfoMapper;
import com.atguigu.gmall.manager.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<SkuInfo> getMySkuInfo(String catalog3Id) {

        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(catalog3Id);
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);

        for (SkuInfo info : skuInfos) {
            String skuId = info.getId();

            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);

            info.setSkuAttrValueList(skuAttrValues);
        }

        return skuInfos;
    }

    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = redisUtil.getJedis();
        //从缓存中获取数据
        String cacheJson = jedis.get("sku:" + skuId + ":info");
        //if数据为空
        if(StringUtils.isBlank(cacheJson)){
            //获取分布式锁 NX ：只在键不存在时，才对键进行设置操作 px：过期毫秒数
            String OK = jedis.set("sku:" + skuId + ":lock", "1", "nx", "px", 1000);
            if(StringUtils.isBlank(OK)){
                //从数据库查询
                skuInfo = getSkuInfoBySkuIdFromDB(skuId);
                if (skuInfo != null) {
                    //把数据存入缓存
                    String set = jedis.set("sku:" + skuId + ":info", JSON.toJSONString(skuInfo));
                }
                //删除锁
                jedis.del("sku:" + skuId + ":lock");
            }else{
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //自旋（等待锁）
                return getSkuInfoBySkuId(skuId);
            }
        }else{
            //解析缓存中取出来的数据
            skuInfo = JSON.parseObject(cacheJson, SkuInfo.class);
        }

        jedis.close();

        return skuInfo;
    }


    public SkuInfo getSkuInfoBySkuIdFromDB(String skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> imageList = skuImageMapper.select(skuImage);

        skuInfo.setSkuImageList(imageList);

        return skuInfo;
    }

    @Override
    public List<SkuInfo> skuSaleAttrValueListBySpu(String spuId) {

        return skuInfoMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<SkuInfo> getSkuInfoListBySpuId(String spuId) {

        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);

        return skuInfoMapper.select(skuInfo);
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {

        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }
    }
}
