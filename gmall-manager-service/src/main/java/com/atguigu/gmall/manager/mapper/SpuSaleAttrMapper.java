package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr>{

    List<SpuSaleAttr> selectSpuSaleAttrListBySpuId(@Param("spuId") String spuId, @Param("skuId") String skuId);
}
