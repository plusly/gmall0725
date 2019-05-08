package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.Constant;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.list.utils.MyDsl;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {
        //得到查询sql
        String dsl = MyDsl.getDsl(skuLsParam);
        //创建查询对象
        Search build = new Search.Builder(dsl).addIndex(Constant.ELASTICSEARCH_INDEX).addType(Constant.ELASTICSEARCH_TYPE).build();
        //获得查询结果
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //得到hit
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
        //遍历取出source，添加到SkuLsInfo集合中
        ArrayList<SkuLsInfo> skuLsInfos = new ArrayList<>();
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {

            SkuLsInfo source = hit.source;
            //替换查询关键字，让其高亮显示
            if (hit.highlight != null) {

                Map<String, List<String>> highlight = hit.highlight;
                List<String> strings = highlight.get("skuName");
                String s = strings.get(0);

                source.setSkuName(s);
            }

            skuLsInfos.add(source);
        }

        //取聚合函数对象
        List<String> attrValueIdList=new ArrayList<>();
        MetricAggregation aggregations = execute.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        if (groupby_attr != null) {

            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                //去聚合函数中的valueId
                attrValueIdList.add(bucket.getKey());//聚合的字段值，属性值id
                Long count = bucket.getCount();//聚合字段出现的次数
            }
        }

        return skuLsInfos;
    }
}
