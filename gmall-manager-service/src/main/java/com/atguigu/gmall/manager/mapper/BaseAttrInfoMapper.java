package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo>{
    List<BaseAttrInfo> selectAttrListByValueIds(Map<String,String> map);
}
