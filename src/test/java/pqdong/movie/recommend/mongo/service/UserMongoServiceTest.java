package pqdong.movie.recommend.mongo.service;

import cn.hutool.Hutool;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import javafx.scene.input.DataFormat;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Before;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import pqdong.movie.recommend.mongo.model.recom.Recommendation;
import pqdong.movie.recommend.temp.PersonTemp;
import pqdong.movie.recommend.service.mabatis.impl.PersonMybatisServiceImpl;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pqdong.movie.recommend.data.entity.Comments;
import pqdong.movie.recommend.data.entity.Movie;
import pqdong.movie.recommend.data.entity.Rating;
import pqdong.movie.recommend.data.repository.PersonRepository;
import pqdong.movie.recommend.mapper.CommentsMapper;
import pqdong.movie.recommend.mapper.MovieMapper;
import pqdong.movie.recommend.mapper.RatingMapper;
import pqdong.movie.recommend.mapper.UserMapper;
import pqdong.movie.recommend.mongo.model.domain.User;
import pqdong.movie.recommend.mongo.utils.Constant;
import pqdong.movie.recommend.service.mabatis.CommentsService;
import pqdong.movie.recommend.service.mabatis.MovieMybatisService;
import pqdong.movie.recommend.service.mabatis.RatingMybatisService;

import pqdong.movie.recommend.temp.CommentsTemp;
import pqdong.movie.recommend.temp.MovieTemp;
import pqdong.movie.recommend.temp.RatingTemp;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.EsUtuils;
import pqdong.movie.recommend.utils.RandomStringUtils;

import javax.annotation.Resource;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.index.query.BoolQueryBuilder;


/**
 * @author Mr.Wang
 * @create 2025-04-16-9:28
 */
@SpringBootTest
@Slf4j
class UserMongoServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UserMongoServiceTest.class);

    @Resource
    private CommentsMapper commentsMapper;
    @Resource
    private RatingMapper ratingMapper;
    @Resource
    private MovieMapper movieMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private MovieMybatisService movieMybatisService;
    @Resource
    private RatingMybatisService ratingMybatisService;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private PersonMybatisServiceImpl personMybatisService;

    @Resource
    private PersonRepository personRepository;

    private MongoCollection<Document> userCollection;

    private MongoCollection<Document> personCollection;

    private MongoCollection<Document> commentCollection;
    private MongoCollection<Document> ratingCllection;

    private MongoCollection<Document> movieCllection;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    private MongoCollection<Document> getMovieCollection() {
        if (null == movieCllection)
            movieCllection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Movie");
        return movieCllection;
    }

    private MongoCollection<Document> getUserCollection() {
        if (null == userCollection)
            userCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_COLLECTION);
        return userCollection;
    }

    private MongoCollection<Document> getRatingCollection() {
        if (null == ratingCllection)
            ratingCllection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Rating");
        return ratingCllection;
    }

    private MongoCollection<Document> getPersonCollection() {
        if (null == personCollection)
            personCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Person");
        return personCollection;
    }

    private MongoCollection<Document> getCommentCollection() {
        if (null == commentCollection)
            commentCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Comment");
        return commentCollection;
    }

    @Resource
    private UserMongoService userMongoService;

    @Test
    void del() {
        // 1. 获取集合（确保已初始化）
        MongoCollection<Document> userCollection = getUserCollection();

        // 2. 执行删除所有文档（空过滤器表示匹配全部）
        DeleteResult result = userCollection.deleteMany(new Document());

        // 3. 打印删除结果（调试用）
        System.out.println("Deleted count: " + result.getDeletedCount());
    }

    @Test
    void registerUser() {

        for (int i = 1; i <= 5000; i++) {
            String[] type = {"Crime", "Drama", "Film-Noir", "Thriller"};
            User user = new User();
            String username = RandomStringUtils.generate();
            user.setUsername(username + i);
            user.setPassword("12345678");
            user.setFirst(true);
            user.setUid(i);
            user.setPrefGenres(Arrays.asList(type));
            user.setTimestamp(System.currentTimeMillis());
            user.setUserRole("user");
            try {

                getUserCollection().insertOne(Document.parse(objectMapper.writeValueAsString(user)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }

    }

    @Test
    void savePersonToMongo() {
        MongoCollection<Document> personCollection1 = getPersonCollection();
        List<PersonTemp> list = personMybatisService.list();
        List<Document> collect = list.stream().map(personTemp -> Document.parse(JSONUtil.toJsonStr(personTemp))).collect(Collectors.toList());
        personCollection1.insertMany(collect);
    }

    @Test
    void saveCommentToMongo() {
        MongoCollection<Document> personCollection1 = getCommentCollection();
        List<Comments> list = commentsService.list().subList(1, 10000);
        List<Document> collect = list.stream().map(person -> Document.parse(JSONUtil.toJsonStr(person))).collect(Collectors.toList());
        personCollection1.insertMany(collect);
    }

    @Test
    void delAllPerson() {
        // 1. 获取集合（确保已初始化）
        MongoCollection<Document> userCollection = getPersonCollection();

        // 2. 执行删除所有文档（空过滤器表示匹配全部）
        DeleteResult result = userCollection.deleteMany(new Document());

        // 3. 打印删除结果（调试用）
        System.out.println("Deleted count: " + result.getDeletedCount());
    }

    //迁移目前的评分数据
    @Test
    void saveRatingToMongo() {
        List<Rating> list = ratingMapper.selectNumRating();
        List<RatingTemp> ratingTemps = list.stream().map(rating -> {
            RatingTemp ratingTemp = new RatingTemp();
            ratingTemp.setUserId(Integer.valueOf(String.valueOf(rating.getUserId())));
            ratingTemp.setMovieId(Integer.valueOf(String.valueOf(rating.getMovieId())));
            ratingTemp.setRating(rating.getRating());
            ratingTemp.setTimestamp((int) (rating.getRatingTime().getTime() / 1000));
            return ratingTemp;

        }).collect(Collectors.toList());
        MongoCollection<Document> personCollection1 = getRatingCollection();
        List<Document> collect = ratingTemps.stream().map(person -> Document.parse(JSONUtil.toJsonStr(person))).collect(Collectors.toList());
        personCollection1.insertMany(collect);
    }

    @Test
    void delAllRating() {
        // 1. 获取集合（确保已初始化）
        MongoCollection<Document> userCollection = getRatingCollection();

        // 2. 执行删除所有文档（空过滤器表示匹配全部）
        DeleteResult result = userCollection.deleteMany(new Document());

        // 3. 打印删除结果（调试用）
        System.out.println("Deleted count: " + result.getDeletedCount());
    }

    @Test
    void saveMovieToMongo() {
        List<Movie> list = movieMybatisService.list().subList(1, 1500);
        MongoCollection<Document> personCollection1 = getMovieCollection();
        List<Document> collect = list.stream().map(person -> Document.parse(JSONUtil.toJsonStr(person))).collect(Collectors.toList());
        personCollection1.insertMany(collect);
    }

    //换个做法，我们将电影现在可用的频分对应的useId，movie反向更新好就可以了
    @Test
    void updateUserMovie() {
        // 1. 获取Rating集合实例


        MongoCollection<Document> ratingCollection = getRatingCollection();

        // 2. 执行全量查询（无过滤条件）
        List<Document> allRatings = ratingCollection.find().into(new ArrayList<>());

        // 3. 打印结果（调试用）
//        System.out.println("Total ratings count: " + allRatings.size());
//        allRatings.forEach(doc ->
//                System.out.println("Rating Document: " + doc.toJson())
//        );

        // 4. 可选：转换为RatingTemp实体列表（根据业务需要）
        List<RatingTemp> ratingList = allRatings.stream()
                .map(doc -> objectMapper.convertValue(doc, RatingTemp.class))
                .collect(Collectors.toList());
        System.out.println();

        List<Integer> userIds = ratingList.stream().map(RatingTemp::getUserId).distinct().collect(Collectors.toList());
        List<Integer> movieIds = ratingList.stream().map(RatingTemp::getMovieId).distinct().collect(Collectors.toList());
        System.out.println("userIds:" + userIds.size() + "======movieIds:" + movieIds.size());
        HashMap<Integer, Integer> userIdMap = new HashMap<>();
        HashMap<Integer, Integer> movieIdMap = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            userIdMap.put(i, userIds.get(i));

        }
        for (int i = 0; i < movieIds.size(); i++) {
            movieIdMap.put(i, movieIds.get(i));

        }
        System.out.println("======================访问mysql与数据转换====================");
        List<Movie> movies = movieMapper.selectLimit(movieIds.size());
        List<Comments> comments = commentsMapper.selectCommentsByRating();
        List<MovieTemp> resMovies = new ArrayList<>();
        List<CommentsTemp> resComments = new ArrayList<>();
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            MovieTemp movieTemp = BeanUtil.copyProperties(movie, MovieTemp.class);
            movieTemp.setMovieId(movieIdMap.get(i));
            resMovies.add(movieTemp);
            int j = i;
            comments.stream().forEach(comments1 -> {
                if (comments1.getMovieId().equals(movie.getMovieId())) {
                    CommentsTemp commentsTemp = BeanUtil.copyProperties(comments1, CommentsTemp.class);
                    commentsTemp.setMovieId(movieIdMap.get(j));
                    resComments.add(commentsTemp);
                }
            });
        }
//        处理user
        List<UserTemp> resUsers = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            UserTemp userTemp = new UserTemp();
            userTemp.setUserId(movieIdMap.get(i));
            userTemp.setPassword("035e7473d0c5e0bd762acbb2c26df841");
            userTemp.setUserAvatar("http://oimageb2.ydstatic.com/image?id=8981431901149412470&product=bisheng");
            String name = RandomStringUtils.generate();
            userTemp.setUserNickname(name + i);
            userTemp.setSex("男");
            userTemp.setUserRole("user");
            userTemp.setCreateTime(System.currentTimeMillis() + "");
            userTemp.setUpdateTime(System.currentTimeMillis() + "");

            resUsers.add(userTemp);
        }
        //保存到Mogo
        System.out.println("======================保存到mongo====================");

        List<Document> userDoc = resUsers.stream().map(userTemp -> Document.parse(JSONUtil.toJsonStr(userTemp))).collect(Collectors.toList());
        getUserCollection().insertMany(userDoc);
        List<Document> movieDoc = resMovies.stream().map(movieTemp -> Document.parse(JSONUtil.toJsonStr(movieTemp))).collect(Collectors.toList());
        getMovieCollection().insertMany(movieDoc);
        List<Document> commentsDoc = resComments.stream().map(commentsTemp -> Document.parse(JSONUtil.toJsonStr(commentsTemp))).collect(Collectors.toList());
        getCommentCollection().insertMany(commentsDoc);

        System.out.println("======================数据完成====================");


    }

    //    提取所有的电影标签类型，用于冷启动时可以给用户选择自己喜爱的类型
    @Test
    void getAllGeners() {
        MongoCollection<Document> movieCollection = getMovieCollection();

        // 使用HashSet自动去重
        HashMap<String, Integer> allGeners = new HashMap<String, Integer>();

        // 查询所有包含genres字段的文档
        FindIterable<Document> documents = movieCollection.find(
                new Document("genres", new Document("$exists", true)) // 确保字段存在
        );

        // 遍历文档并提取类型
        for (Document doc : documents) {
            String genres = doc.getString("genres");
            if (genres != null && !genres.isEmpty()) {
                // 分割字符串并清理空格
                String[] splitGeners = genres.split("/");
                for (String genre : splitGeners) {
                    String cleanedGenre = genre.trim();
                    if (!cleanedGenre.isEmpty()) {
                        allGeners.put(cleanedGenre, allGeners.getOrDefault(cleanedGenre, 0) + 1);
                    }
                }
            }
        }
// 使用Stream API按值降序排序
        List<Map.Entry<String, Integer>> sortedEntries = allGeners.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

// 转换为有序Map（LinkedHashMap保持插入顺序）
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        sortedEntries.forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        Set<String> genersSet = sortedMap.keySet();
        genersSet.forEach((item) -> {
            System.out.print(item + " ");
        });
    }

    //    将mongo中的movie数据导入到es
    @Test
    public void movieToEs() throws Exception {
        try {
            // 从MongoDB获取电影数据
            MongoCollection<Document> movieCollection = getMovieCollection();
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.timeout("2m");

            int count = 0;
            for (Document doc : movieCollection.find()) {
                try {
                    // 处理movieId的类型转换
                    Object movieIdObj = doc.get("movieId");
                    String movieId = movieIdObj instanceof Integer ?
                            String.valueOf(movieIdObj) :
                            movieIdObj.toString();

                    // 打印文档内容用于调试
                    logger.info("正在处理第 {} 条数据，movieId: {}", ++count, movieId);

                    // 创建新的文档，排除_id和releaseDate字段
                    Document esDoc = new Document(doc);
                    esDoc.remove("_id");
                    esDoc.remove("releaseDate");

                    logger.debug("文档内容: {}", esDoc.toJson());

                    IndexRequest indexRequest = new IndexRequest("movies")
                            .id(movieId)
                            .source(esDoc.toJson(), XContentType.JSON);
                    bulkRequest.add(indexRequest);
                } catch (Exception e) {
                    logger.error("处理文档时发生错误，movieId: {}, 错误信息: {}",
                            doc.get("movieId"), e.getMessage());
                    throw e;
                }
            }

            // 执行批量导入
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.hasFailures()) {
                // 打印每个失败项的详细信息
                for (BulkItemResponse bulkItemResponse : bulkResponse) {
                    if (bulkItemResponse.isFailed()) {
                        BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                        logger.error("文档 {} 导入失败: {}",
                                bulkItemResponse.getId(),
                                failure.getMessage());
                    }
                }
                logger.error("批量导入失败，总失败数: {}",
                        bulkResponse.getItems().length - bulkResponse.getItems().length);
            } else {
                logger.info("成功导入 {} 条电影数据", bulkResponse.getItems().length);
            }
        } catch (Exception e) {
            logger.error("导入电影数据到ES时发生错误: ", e);
            throw e;
        }
    }

    @Test
    public void getMoviesFromEs() throws Exception {
        try {

            // 1. 准备Request对象
            GetRequest getRequest = new GetRequest("movies", "1"); // 查询ID为1的文档

            // 2. 发送请求，得到结果
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

            // 3. 解析结果
            if (getResponse.isExists()) {
                String sourceAsString = getResponse.getSourceAsString();
                logger.info("找到文档，内容: {}", sourceAsString);
            } else {
                logger.info("未找到ID为1的文档");
            }

        } catch (Exception e) {
            logger.error("从ES获取电影数据时发生错误: ", e);
            throw e;
        }
    }

//    @Test
//    public void getMoviesByGenres() throws Exception {
//        // 1. 创建搜索请求
//        SearchRequest request = new SearchRequest("movies");
//
//        // 2. 构建查询（使用bool查询和should子句）
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        // 添加多个should条件，每个标签一个条件
////        喜剧/动作/爱情/歌舞/家庭/冒险/运动
//        boolQueryBuilder.should(QueryBuilders.matchQuery("genres", "喜剧"));
//        boolQueryBuilder.should(QueryBuilders.matchQuery("genres", "恐怖"));
//        boolQueryBuilder.should(QueryBuilders.matchQuery("genres", "惊悚"));
//        boolQueryBuilder.should(QueryBuilders.matchQuery("genres", "歌舞"));
//        boolQueryBuilder.should(QueryBuilders.matchQuery("genres", "家庭"));
//        boolQueryBuilder.should(QueryBuilders.matchQuery("genres", "冒险"));
//        boolQueryBuilder.should(QueryBuilders.matchQuery("genres", "运动"));
//
//        // 设置最小匹配数（可选，这里设置为1表示至少匹配一个标签）
//        boolQueryBuilder.minimumShouldMatch(1);
//
//        // 3. 设置查询条件
//        request.source()
//            .query(boolQueryBuilder)
//            .sort(SortBuilders.scoreSort().order(SortOrder.DESC.DESC)); // 按匹配度降序排序
//
//        // 4. 执行查询
//        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
//
//        // 5. 处理响应
//        handleResponse(response);
//    }

//    private void handleResponse(SearchResponse response) {
//        // 4.解析响应
//        SearchHits searchHits = response.getHits();
//        // 4.1.获取总条数
//        long total = searchHits.getTotalHits().value;
//        System.out.println("共搜索到" + total + "条数据");
//        // 4.2.文档数组
//        SearchHit[] hits = searchHits.getHits();
//        // 4.3.遍历
//        for (SearchHit hit : hits) {
//            // 获取文档source
//            String json = hit.getSourceAsString();
//            // 反序列化
//            MovieTemp movieTemp = JSON.parseObject(json, MovieTemp.class);
//            System.out.println("电影信息: " + JSONUtil.toJsonStr(movieTemp));
//            System.out.println("匹配分数: " + hit.getScore()); // 打印匹配分数
//        }
//    }

    @Resource
    private EsUtuils esUtuils;

    @Test
    public void testSearchMoviesByGenres() throws Exception {
        // 测试用例
        List<String> genres = Arrays.asList("喜剧", "恐怖", "惊悚", "歌舞", "家庭", "冒险", "运动");
        int num = 5; // 只返回评分最高的5条数据
        List<Recommendation> recommendations = esUtuils.searchMoviesByGenres(genres, num);

//        // 打印结果
//        for (MovieTemp movie : movies) {
//            logger.info("找到电影: {}", JSONUtil.toJsonStr(movie));
//        }
    }

    /**
     * 根据电影类型列表搜索电影
     *
     * @param genres 电影类型列表
     * @param num    返回结果的最大数量
     * @return 搜索结果列表
     */
    public List<MovieTemp> searchMoviesByGenres(List<String> genres, int num) throws Exception {
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
                .sort(SortBuilders.scoreSort().order(SortOrder.DESC)) // 按匹配度降序排序
                .size(num); // 设置返回结果数量

        // 4. 执行查询
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        // 5. 处理响应并返回结果
        return handleResponse(response);
    }

    private List<MovieTemp> handleResponse(SearchResponse response) {
        // 4.解析响应
        SearchHits searchHits = response.getHits();
        // 4.1.获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到" + total + "条数据");
        // 4.2.文档数组
        SearchHit[] hits = searchHits.getHits();
        // 4.3.遍历
        List<MovieTemp> movieTemps = new ArrayList<>();
        for (SearchHit hit : hits) {
            // 获取文档source
            String json = hit.getSourceAsString();
            // 反序列化
            MovieTemp movieTemp = JSON.parseObject(json, MovieTemp.class);
            movieTemps.add(movieTemp);
        }
        return movieTemps;
    }

    //    将movie表中的发行时间时间戳改为int
    @Test
    void updateMovieReleaseDate() {
        try {
            // 1. 获取MongoDB中的所有电影数据
            MongoCollection<Document> movieCollection = getMovieCollection();
            FindIterable<Document> mongoMovies = movieCollection.find();

            // 2. 遍历MongoDB中的每部电影
            for (Document movieDoc : mongoMovies) {
                String movieName = movieDoc.getString("name");
                if (movieName == null || movieName.isEmpty()) {
                    logger.warn("发现电影名称为空的记录，跳过处理");
                    continue;
                }

                // 3. 根据电影名称从MySQL查询对应的电影
                QueryWrapper<Movie> wrapper = new QueryWrapper<>();
                wrapper.eq("name", movieName);
                List<Movie> list = movieMybatisService.list(wrapper);
                Movie mysqlMovie = list.get(0);
                if (mysqlMovie == null || mysqlMovie.getReleaseDate() == null) {
                    logger.warn("MySQL中未找到电影 {} 的发行日期信息，跳过处理", movieName);
                    continue;
                }

                // 4. 将MySQL中的日期转换为时间戳
//            DateTime dateTime = DateTime.parse(mysqlMovie.getReleaseDate(), new DataFormat("EEE MMM dd HH:mm:ss z yyyy"));
                // 将解析后的DateTime对象格式化为所需的字符串形式
//            String formattedTime = dateTime.format(Pattern.create("yyyy-MM-dd"));
                long timestamp = mysqlMovie.getReleaseDate().getTime();
                int finalTimestamp = (int) (timestamp / 1000); // 转换为秒级时间戳

                if (finalTimestamp < 0) {
                    System.out.println("小于0");
                }
                // 5. 更新MongoDB中的releaseDate字段
                Document updateDoc = new Document("$set",
                        new Document("releaseDate", finalTimestamp));

                movieCollection.updateOne(
                        new Document("movieId", movieDoc.get("movieId")),
                        updateDoc
                );

                logger.info("更新电影 {} 的发行日期成功，原始日期: {}，更新后的时间戳: {}",
                        movieName,
                        mysqlMovie.getReleaseDate(),
                        finalTimestamp);
            }

            logger.info("所有电影发行日期更新完成");

        } catch (Exception e) {
            logger.error("更新电影发行日期时发生错误: ", e);
            throw e;
        }
    }

}