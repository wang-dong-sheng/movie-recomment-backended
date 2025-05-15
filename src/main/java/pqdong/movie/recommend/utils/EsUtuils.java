package pqdong.movie.recommend.utils;/**
 * @author Mr.Wang
 * @create 2025-04-20-16:04
 */

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;
import pqdong.movie.recommend.data.dto.Recommendation.Recommendation;
import pqdong.movie.recommend.data.entity.MovieTemp;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *@ClassName EsUtuils
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/4/20 16:04
 *@Version 1.0
 */
@Component
@Slf4j
public class EsUtuils {
    @Resource
    private RestHighLevelClient restHighLevelClient;
    /**
     * 根据电影类型列表搜索电影
     * @param genres 电影类型列表
     *  @param  num:返回几条数据
     * @return 搜索结果列表
     */
    public List<Recommendation> searchMoviesByGenres(List<String> genres, int num) {
        try {
            // 1. 创建搜索请求
            SearchRequest request = new SearchRequest("movies");

            // 2. 构建查询（使用bool查询和should子句）
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            // 添加多个should条件，每个标签一个条件
            for (String genre : genres) {
                boolQueryBuilder.should(QueryBuilders.matchQuery("genres", genre));
            }

            // 设置最小匹配数（可选，这里设置为1表示至少匹配一个标签）
            boolQueryBuilder.minimumShouldMatch(1);

            // 3. 设置查询条件
            request.source()
                    .query(boolQueryBuilder)
                    .sort(SortBuilders.scoreSort().order(SortOrder.DESC)).size(num); // 按匹配度降序排序

            // 4. 执行查询
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            // 5. 处理响应并返回结果
            return handleResponse(response);
        }catch (Exception e){

        }
       return new ArrayList<>();
    }

    /**
     * 处理搜索结果
     * @param response 搜索响应
     * @return 电影列表
     */
    private List<Recommendation> handleResponse(SearchResponse response) {
        List<Recommendation> result = new ArrayList<>();

        // 解析响应
        SearchHits searchHits = response.getHits();
        // 获取总条数
        long total = searchHits.getTotalHits().value;
        log.info("共搜索到{}条数据", total);

        // 文档数组
        SearchHit[] hits = searchHits.getHits();
        // 遍历
        for (SearchHit hit : hits) {
            Recommendation recommendation = new Recommendation();
            // 获取文档source
            String json = hit.getSourceAsString();
            // 反序列化
            MovieTemp movieTemp = JSON.parseObject(json, MovieTemp.class);
            recommendation.setMid(movieTemp.getMovieId());
            recommendation.setScore((double) hit.getScore());
            log.info("电影信息: {}", JSONUtil.toJsonStr(movieTemp));
            log.info("匹配分数: {}",hit.getScore());
            result.add(recommendation);
        }

        return result;
    }

}


