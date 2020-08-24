package com.lzq.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lzq.pojo.Content;
import com.lzq.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient client;

    /**
     *   1.解析数据放入es索引库中
     */
    public Boolean parseContent(String keywords) throws Exception {
        List<Content> list = new HtmlParseUtil().parseJD(keywords);
        //把查询出来的数据放入到es里面
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_goods")
            .source(JSON.toJSONString(list.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    public ArrayList<Map<String,Object>> getEstic(Integer pageNo, Integer pageSize, String searchKey)
            throws IOException {
        if (pageNo <= 1) {
            pageNo = 1;
        }
    // 条件搜索
    SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        // 精准匹配
        MatchQueryBuilder title = QueryBuilders.matchQuery("title",searchKey);
        sourceBuilder.query(title);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

        // 解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
      /*list.add((Content) hit.getSourceAsMap());*/
            list.add(hit.getSourceAsMap());
        }
        return list;
    }



    public ArrayList<Map<String,Object>> searchPageHighlightBulider(Integer pageNo, Integer pageSize, String searchKey)
            throws IOException {
        if (pageNo <= 1) {
            pageNo = 1;
        }
        // 条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        // 精准匹配
        MatchQueryBuilder title = QueryBuilders.matchQuery("title",searchKey);
        sourceBuilder.query(title);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        //多个高亮显示
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

        // 解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
            HighlightField title1 = highlightFieldMap.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            if (title1!=null){
                Text[] fragments = title1.getFragments();
                String n_title = "";
                for (Text  text:fragments ) {
                    n_title += text;
                }
                sourceAsMap.put("title",n_title);
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
