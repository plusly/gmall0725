package com.atguigu.gmall.list.utils;

import com.atguigu.gmall.bean.SkuLsParam;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;

public class MyDsl {

    public static String getDsl(SkuLsParam skuLsParam){

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if(skuLsParam.getKeyword() != null){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParam.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            //高亮显示
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red;font-weight:boloder'>");
            highlightBuilder.field("skuName");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlight(highlightBuilder);
        }

//        if(skuLsParam.getCatalog3Id() != null){
//            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParam.getCatalog3Id());
//            boolQueryBuilder.filter(termQueryBuilder);
//        }
        String[] valueIds = skuLsParam.getValueId();
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        searchSourceBuilder.size(skuLsParam.getPageSize());
        searchSourceBuilder.from(skuLsParam.getPageSize() * (skuLsParam.getPageNo() - 1));
        //加聚合函数
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        return searchSourceBuilder.toString();

    }
}
