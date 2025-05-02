package pqdong.movie.recommend.mongo.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.data.dto.movie.RecommendVo;
import pqdong.movie.recommend.mongo.model.recom.Recommendation;
import pqdong.movie.recommend.mongo.model.request.*;
import pqdong.movie.recommend.mongo.utils.Constant;
import pqdong.movie.recommend.newService.MovieNewService;
import pqdong.movie.recommend.temp.MovieTemp;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.EsUtuils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class RecommenderService {


    // 混合推荐中CF的比例
    private static Double CF_RATING_FACTOR = 0.3;
    private static Double CB_RATING_FACTOR = 0.3;
    private static Double SR_RATING_FACTOR = 0.4;

    @Autowired
    private MongoClient mongoClient;
    @Resource
    private MovieNewService movieNewService;
    @Resource
    private EsUtuils esUtuils;
    @Resource
    private ObjectMapper objectMapper;
//    @Autowired
//    private TransportClient esClient;

    // 协同过滤推荐【电影相似性】
    private List<Recommendation> findMovieCFRecs(int mid, int maxItems) {
        MongoCollection<Document> movieRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_MOVIE_RECS_COLLECTION);
        Document movieRecs = movieRecsCollection.find(new Document("movieId", mid)).first();
        return parseRecs(movieRecs, maxItems);
    }

    // 协同过滤推荐【用户电影矩阵】
    private List<Recommendation> findUserCFRecs(int uid, int maxItems) {
        MongoCollection<Document> movieRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_RECS_COLLECTION);
        Document userRecs = movieRecsCollection.find(new Document("userId", uid)).first();
        return parseRecs(userRecs, maxItems);
    }



    // 基于内容的推荐算法
//    private List<Recommendation> findContentBasedMoreLikeThisRecommendations(int mid, int maxItems) {
//        MoreLikeThisQueryBuilder query = QueryBuilders.moreLikeThisQuery(/*new String[]{"name", "descri", "genres", "actors", "directors", "tags"},*/
//                new MoreLikeThisQueryBuilder.Item[]{new MoreLikeThisQueryBuilder.Item(Constant.ES_INDEX, Constant.ES_MOVIE_TYPE, String.valueOf(mid))});
//
//        return parseESResponse(esClient.prepareSearch().setQuery(query).setSize(maxItems).execute().actionGet());
//    }

    // 实时推荐
    public List<Recommendation> findStreamRecs(int uid,int maxItems){
        MongoCollection<Document> streamRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_STREAM_RECS_COLLECTION);
        Document streamRecs = streamRecsCollection.find(new Document("userId", uid)).first();
        return parseRecs(streamRecs, maxItems);
    }

    private List<Recommendation> parseRecs(Document document, int maxItems) {
        List<Recommendation> recommendations = new ArrayList<>();
        if (null == document || document.isEmpty())
            return recommendations;
        ArrayList<Document> recs = document.get("recs", ArrayList.class);
        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("movieId"), recDoc.getDouble("score")));
        }
        Collections.sort(recommendations, new Comparator<Recommendation>() {
            @Override
            public int compare(Recommendation o1, Recommendation o2) {
                return o1.getScore() > o2.getScore() ? -1 : 1;
            }
        });
        return recommendations.subList(0, maxItems > recommendations.size() ? recommendations.size() : maxItems);
    }

    // 全文检索
//    private List<Recommendation> findContentBasedSearchRecommendations(String text, int maxItems) {
//        MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(text, "name", "descri");
//        return parseESResponse(esClient.prepareSearch().setIndices(Constant.ES_INDEX).setTypes(Constant.ES_MOVIE_TYPE).setQuery(query).setSize(maxItems).execute().actionGet());
//    }

    private List<Recommendation> parseESResponse(SearchResponse response) {
        List<Recommendation> recommendations = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            recommendations.add(new Recommendation((int) hit.getSourceAsMap().get("movieId"), (double) hit.getScore()));
        }
        return recommendations;
    }

    // 混合推荐算法
    private List<Recommendation> findHybridRecommendations(int userId, int maxItems) {
        List<Recommendation> hybridRecommendations = new ArrayList<>();
////基于物品的
//        List<Recommendation> cfRecs = findMovieCFRecs(productId, maxItems);
//        for (Recommendation recommendation : cfRecs) {
//            hybridRecommendations.add(new Recommendation(recommendation.getMid(), recommendation.getScore() * CF_RATING_FACTOR));
//        }
//        基于用户的
        List<Recommendation> userCFRecs = findUserCFRecs(userId, maxItems);
        for (Recommendation recommendation : userCFRecs) {
            hybridRecommendations.add(new Recommendation(recommendation.getMid(),recommendation.getScore()*CF_RATING_FACTOR));
        }
        //通过es进行基于内容的推荐，es筛选通过TF-IDF算法来进行筛选，因为es的评分在范围不确定，采取映射方式，将最高匹配的分数的向上取整映射为5分以此来
        //换算
//实时推荐
        List<Recommendation> esContentRecommnt = findEsContentRecommnt(userId, maxItems);
        double rate=3;//映射比例
        if (esContentRecommnt!=null&&!esContentRecommnt.isEmpty()){
            int upRating=(int)(esContentRecommnt.get(0).getScore()+1);
            rate=upRating/5.0;
        }
        for (Recommendation recommendation : esContentRecommnt) {

            hybridRecommendations.add(new Recommendation(recommendation.getMid(), recommendation.getScore()/rate * SR_RATING_FACTOR));
        }
        //基于实时推荐
        List<Recommendation> streamRecs = findStreamRecs(userId,maxItems);
        for (Recommendation recommendation : streamRecs) {
            hybridRecommendations.add(new Recommendation(recommendation.getMid(), recommendation.getScore() * SR_RATING_FACTOR));
        }

        Collections.sort(hybridRecommendations, new Comparator<Recommendation>() {
            @Override
            public int compare(Recommendation o1, Recommendation o2) {
                return o1.getScore() > o2.getScore() ? -1 : 1;
            }
        });
        return hybridRecommendations.subList(0, maxItems > hybridRecommendations.size() ? hybridRecommendations.size() : maxItems);
    }


    /**
     * 使用es基于用户兴趣标签进行基于内容的推荐
     * @param userId
     * @return
     */
    private List<Recommendation> findEsContentRecommnt(int userId,int num){
        MongoCollection<Document> userCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_COLLECTION);
        Document userDoc = userCollection.find(new Document("userId", userId)).first();
        UserTemp user = documentToUser(userDoc);
        List<String> prefGenres = user.getPrefGenres();
        List<Recommendation> recommendations = esUtuils.searchMoviesByGenres(prefGenres, num);

        return recommendations;
    }

    private UserTemp documentToUser(Document document) {
        UserTemp user = null;
        try {
            user = objectMapper.readValue(JSON.serialize(document), UserTemp.class);
        } catch (IOException e) {
            log.error("转换用户文档失败", e);
        }
        return user;
    }
    public List<Recommendation> getCollaborativeFilteringRecommendations(MovieRecommendationRequest request) {
        return findMovieCFRecs(request.getMid(), request.getSum());
    }

    public List<Recommendation> getCollaborativeFilteringRecommendations(UserRecommendationRequest request) {

        return findUserCFRecs(request.getUid(), request.getSum());
    }

//    public List<Recommendation> getContentBasedMoreLikeThisRecommendations(MovieRecommendationRequest request) {
//        return findContentBasedMoreLikeThisRecommendations(request.getMid(), request.getSum());
//    }

//    public List<Recommendation> getContentBasedSearchRecommendations(SearchRecommendationRequest request) {
//        return findContentBasedSearchRecommendations(request.getText(), request.getSum());
//    }

    public List<Recommendation> getHybridRecommendations(RecommendVo request) {
        return findHybridRecommendations(request.getUserId(), request.getNum());
    }


    public List<MovieTemp> getHotRecommendations(HotRecommendationRequest request) {
        // 获取热门电影的条目
        MongoCollection<Document> rateMoreMoviesRecentlyCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_MOVIES_RECENTLY_COLLECTION);
        FindIterable<Document> documents = rateMoreMoviesRecentlyCollection.find().sort(Sorts.descending("yeahmonth")).limit(request.getSum());

        List<Recommendation> recommendations = new ArrayList<>();
        for (Document document : documents) {
            recommendations.add(new Recommendation(document.getInteger("movieId"), 0D));
        }
        List<MovieTemp> recommendeMovies = movieNewService.getRecommendeMovies(recommendations);

        return recommendeMovies;
    }

    public List<Recommendation> getRateMoreRecommendations(RateMoreRecommendationRequest request) {

        // 获取评分最多电影的条目
        MongoCollection<Document> rateMoreMoviesCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATE_MORE_MOVIES_COLLECTION);
        FindIterable<Document> documents = rateMoreMoviesCollection.find().sort(Sorts.descending("count")).limit(request.getSum());

        List<Recommendation> recommendations = new ArrayList<>();
        for (Document document : documents) {
            recommendations.add(new Recommendation(document.getInteger("movieId"), 0D));
        }
        return recommendations;
    }

//    public List<Recommendation> getContentBasedGenresRecommendations(SearchRecommendationRequest request) {
//        FuzzyQueryBuilder query = QueryBuilders.fuzzyQuery("genres", request.getText());
//        return parseESResponse(esClient.prepareSearch().setIndices(Constant.ES_INDEX).setTypes(Constant.ES_MOVIE_TYPE).setQuery(query).setSize(request.getSum()).execute().actionGet());
//    }

    public List<Recommendation> getTopGenresRecommendations(TopGenresRecommendationRequest request){
        Document genresTopMovies = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_GENRES_TOP_MOVIES_COLLECTION)
                .find(Filters.eq("genres",request.getGenres())).first();
        return parseRecs(genresTopMovies,request.getSum());
    }

}