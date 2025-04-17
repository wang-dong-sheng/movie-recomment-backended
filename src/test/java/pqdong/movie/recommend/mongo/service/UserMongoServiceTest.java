package pqdong.movie.recommend.mongo.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pqdong.movie.recommend.data.entity.Comments;
import pqdong.movie.recommend.data.entity.Movie;
import pqdong.movie.recommend.data.entity.PersonEntity;
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
import pqdong.movie.recommend.utils.RandomStringUtils;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.Wang
 * @create 2025-04-16-9:28
 */
@SpringBootTest
class UserMongoServiceTest {
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
    private PersonRepository personRepository;

    private MongoCollection<Document> userCollection;

    private MongoCollection<Document> personCollection;

    private MongoCollection<Document> commentCollection;
    private MongoCollection<Document> ratingCllection;

    private MongoCollection<Document> movieCllection;

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
        if (null == userCollection)
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
        List<PersonEntity> allPerson = personRepository.findAllByCountLimit(1000);
        List<Document> collect = allPerson.stream().map(person -> Document.parse(JSONUtil.toJsonStr(person))).collect(Collectors.toList());
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
            ratingTemp.setRatingTime(rating.getRatingTime());
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
            int j=i;
            comments.stream().forEach(comments1 -> {
                if (comments1.getMovieId().equals(movie.getMovieId())){
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
}