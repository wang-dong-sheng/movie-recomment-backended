package pqdong.movie.recommend.newService;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.common.ErrorCode;
import pqdong.movie.recommend.data.constant.ServerConstant;
import pqdong.movie.recommend.data.dto.movie.MovieQueryRequest;
import pqdong.movie.recommend.data.dto.movie.MovieSearchDto;
import pqdong.movie.recommend.data.dto.movie.MovieUpVo;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequest;
import pqdong.movie.recommend.data.dto.rating.RatingVo;
import pqdong.movie.recommend.domain.service.MovieRecommender;
import pqdong.movie.recommend.enums.MovieRecommentEnum;
import pqdong.movie.recommend.exception.ThrowUtils;
import pqdong.movie.recommend.mongo.model.domain.MovieMongo;
import pqdong.movie.recommend.mongo.utils.Constant;
import pqdong.movie.recommend.redis.RedisApi;
import pqdong.movie.recommend.redis.RedisKeys;
import pqdong.movie.recommend.temp.MovieTemp;
import pqdong.movie.recommend.temp.RatingTemp;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.RecommendUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static pqdong.movie.recommend.data.constant.MovieConstant.RECOMMENT_SIZE;

@Service
@Slf4j
public class MovieNewService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private MovieRecommender movieRecommender;

    @Resource
    private RedisApi redisApi;

    private MongoCollection<Document> movieCollection;
    private MongoCollection<Document> ratingCollection;

    private MongoCollection<Document> getMovieCollection() {
        if (null == movieCollection)
            movieCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Movie");
        return movieCollection;
    }

    private MongoCollection<Document> getRatingCollection() {
        if (null == ratingCollection)
            ratingCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Rating");
        return ratingCollection;
    }

    private MovieTemp documentToMovie(Document document) {
        MovieTemp movie = null;
        try {
            movie = objectMapper.readValue(JSON.serialize(document), MovieTemp.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movie;
    }

    private RatingTemp documentToRating(Document document) {
        RatingTemp rating = null;
        try {
            rating = objectMapper.readValue(JSON.serialize(document), RatingTemp.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rating;
    }

    public Boolean deleteMovies(List<Long> ids) {
        try {
            for (Long id : ids) {
                getMovieCollection().deleteOne(new Document("movieId", id));
            }
            return true;
        } catch (Exception e) {
            log.error("删除电影失败", e);
            return false;
        }
    }

    public Page<MovieTemp> filterMovies(MovieQueryRequest movieQueryRequest) {
        Page<MovieTemp> page = new Page<>(movieQueryRequest.getCurrent(), movieQueryRequest.getPageSize());
        List<MovieTemp> movies = new ArrayList<>();
        
        FindIterable<Document> documents = getMovieCollection().find();
        for (Document document : documents) {
            MovieTemp movie = documentToMovie(document);
            if (movie != null) {
                if (StringUtils.isNotBlank(movieQueryRequest.getName()) && 
                    !movie.getName().contains(movieQueryRequest.getName())) {
                    continue;
                }
                if (movieQueryRequest.getId() != null && 
                    !movie.getMovieId().equals(movieQueryRequest.getId())) {
                    continue;
                }
                if (movieQueryRequest.getTags() != null && !movieQueryRequest.getTags().isEmpty()) {
                    boolean hasAllTags = true;
                    for (String tag : movieQueryRequest.getTags()) {
                        if (movie.getTags() == null || !movie.getTags().contains(tag)) {
                            hasAllTags = false;
                            break;
                        }
                    }
                    if (!hasAllTags) continue;
                }
                movies.add(movie);
            }
        }
        
        page.setTotal(movies.size());
        int fromIndex = (int) ((page.getCurrent() - 1) * page.getSize());
        int toIndex = Math.min(fromIndex + (int) page.getSize(), movies.size());
        page.setRecords(movies.subList(fromIndex, toIndex));
        return page;
    }

    public Boolean isUpMovie(MovieUpVo movieUpVo) {
        try {
            getMovieCollection().updateOne(
                new Document("movieId", movieUpVo.getMovieId()),
                new Document("$set", new Document("isUp", movieUpVo.getTag()))
            );
            return true;
        } catch (Exception e) {
            log.error("更新电影状态失败", e);
            return false;
        }
    }

    public Boolean addMovie(MovieTemp movie) {
        try {
            // 检查电影名是否重复
            Document existing = getMovieCollection().find(new Document("name", movie.getName())).first();
            ThrowUtils.throwIf(existing != null, ErrorCode.OPERATION_ERROR, "当前电影已存在");
            
            getMovieCollection().insertOne(Document.parse(objectMapper.writeValueAsString(movie)));
            return true;
        } catch (Exception e) {
            log.error("添加电影失败", e);
            return false;
        }
    }

    public Boolean updateMovie(MovieTemp movie) {
        try {
            getMovieCollection().updateOne(
                new Document("movieId", movie.getMovieId()),
                new Document("$set", Document.parse(objectMapper.writeValueAsString(movie)))
            );
            return true;
        } catch (Exception e) {
            log.error("更新电影失败", e);
            return false;
        }
    }

    public MovieTemp getMovieById(Long movieId) {
        Document document = getMovieCollection().find(new Document("movieId", movieId)).first();
        if (document == null || document.isEmpty()) {
            return null;
        }
        return documentToMovie(document);
    }

    public RatingVo setScore(RatingVo ratingVo) {
        try {
            // 检查是否已评分
            Document existingRating = getRatingCollection().find(
                new Document("userId", ratingVo.getUserId())
                .append("movieId", ratingVo.getMovieId())
            ).first();

            RatingTemp rating = new RatingTemp();
            rating.setUserId(ratingVo.getUserId().intValue());
            rating.setMovieId(ratingVo.getMovieId().intValue());
            rating.setRating(ratingVo.getRating());
            rating.setRatingTime(new Date());

            if (existingRating != null) {
                // 更新评分
                getRatingCollection().updateOne(
                    new Document("userId", ratingVo.getUserId())
                    .append("movieId", ratingVo.getMovieId()),
                    new Document("$set", Document.parse(objectMapper.writeValueAsString(rating)))
                );
            } else {
                // 新增评分
                getRatingCollection().insertOne(Document.parse(objectMapper.writeValueAsString(rating)));
            }

            // 更新电影平均分
            updateMovieAverageScore(ratingVo.getMovieId());
            
            return ratingVo;
        } catch (Exception e) {
            log.error("评分失败", e);
            return null;
        }
    }

    private void updateMovieAverageScore(Long movieId) {
        List<RatingTemp> ratings = new ArrayList<>();
        FindIterable<Document> documents = getRatingCollection().find(new Document("movieId", movieId));
        for (Document doc : documents) {
            ratings.add(documentToRating(doc));
        }
        
        if (!ratings.isEmpty()) {
            double averageScore = ratings.stream()
                .mapToDouble(RatingTemp::getRating)
                .average()
                .orElse(0.0);
            
            getMovieCollection().updateOne(
                new Document("movieId", movieId),
                new Document("$set", new Document("score", averageScore))
            );
        }
    }

    public RatingTemp getScore(RatingUserRequest ratingUserRequest) {
        Document document = getRatingCollection().find(
            new Document("userId", ratingUserRequest.getUserId())
            .append("movieId", ratingUserRequest.getMovieId())
        ).first();
        
        if (document == null || document.isEmpty()) {
            return null;
        }
        return documentToRating(document);
    }

    public List<MovieTemp> getRecommendMovie(Long userId, MovieRecommentEnum type) {
        List<MovieTemp> recommendMovies = new LinkedList<>();
        String recommend = "";
        
        if (userId != null) {
            // 从缓存加载
            recommend = redisApi.getString(RedisKeys.RECOMMEND + ":" + type.getValue() + ":" + userId);
            if (StringUtils.isEmpty(recommend)) {
                // 获取用户评分记录
                List<RatingTemp> userRatings = getUserRatings(userId);
                if (userRatings != null && !userRatings.isEmpty()) {
                    try {
                        List<Long> movieIds;
                        if (type == MovieRecommentEnum.USER) {
                            movieIds = movieRecommender.userBasedRecommender(userId, RECOMMENT_SIZE);
                        } else if (type == MovieRecommentEnum.CONTENT) {
                            movieIds = movieRecommender.itemBasedRecommender(userId, RECOMMENT_SIZE);
                        } else {
                            movieIds = movieRecommender.itemBaseLike(userId, RECOMMENT_SIZE);
                        }
                        
                        if (movieIds != null && !movieIds.isEmpty()) {
                            recommendMovies.addAll(getMoviesByIds(movieIds));
                        }
                    } catch (Exception e) {
                        log.error("获取推荐电影失败", e);
                    }
                }
            } else {
                recommendMovies.addAll(JSONObject.parseArray(recommend, MovieTemp.class));
            }
        } else {
            // 未登录用户，返回高分电影
            recommendMovies.addAll(getHighScoreMovies(RECOMMENT_SIZE));
        }
        
        // 缓存推荐结果
        if (StringUtils.isEmpty(recommend) && userId != null) {
            redisApi.setValue(
                RedisKeys.RECOMMEND + ":" + type.getValue() + ":" + userId,
                JSONObject.toJSONString(recommendMovies),
                1,
                TimeUnit.DAYS
            );
        }
        
        return recommendMovies;
    }

    private List<RatingTemp> getUserRatings(Long userId) {
        List<RatingTemp> ratings = new ArrayList<>();
        FindIterable<Document> documents = getRatingCollection().find(new Document("userId", userId));
        for (Document doc : documents) {
            ratings.add(documentToRating(doc));
        }
        return ratings;
    }

    private List<MovieTemp> getMoviesByIds(List<Long> movieIds) {
        List<MovieTemp> movies = new ArrayList<>();
        FindIterable<Document> documents = getMovieCollection().find(
            new Document("movieId", new Document("$in", movieIds))
        );
        for (Document doc : documents) {
            movies.add(documentToMovie(doc));
        }
        return movies;
    }

    private List<MovieTemp> getHighScoreMovies(int limit) {
        List<MovieTemp> movies = new ArrayList<>();
        FindIterable<Document> documents = getMovieCollection().find()
            .sort(Sorts.descending("score"))
            .limit(limit);
        for (Document doc : documents) {
            movies.add(documentToMovie(doc));
        }
        return movies;
    }
    public Map<String, Object> getAllMovie(String key, int page, int size) {
        List<MovieTemp> allMovies = new ArrayList<>();
        if (StringUtils.isBlank(key)) {
            FindIterable<Document> documents = getMovieCollection().find()
                    .sort(Sorts.descending("score"))
                    .limit(page * size);
            for (Document doc : documents) {
                allMovies.add(documentToMovie(doc));
            }
        } else {
            FindIterable<Document> documents = getMovieCollection().find(
                            new Document("name", new Document("$regex", key))
                    ).sort(Sorts.descending("score"))
                    .limit(page * size);
            for (Document doc : documents) {
                allMovies.add(documentToMovie(doc));
            }
        }

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allMovies.size());
        List<MovieTemp> movieList = allMovies.subList(fromIndex, toIndex);

        Map<String, Object> result = new HashMap<>(2, 1);
        result.put("total", allMovies.size());
        result.put("movieList", movieList.stream()
                .peek(m -> {
                    if (StringUtils.isEmpty(m.getCover())) {
                        m.setCover(ServerConstant.DefaultImg);
                    }
                })
                .collect(Collectors.toCollection(LinkedList::new)));
        return result;
    }
// ... existing code ...

    public Map<String, Object> searchMovies(MovieSearchDto info) {
        List<MovieTemp> allMovies = new ArrayList<>();

        // 按标签搜索
        if (info.getTags() != null && !info.getTags().isEmpty()) {
            for (String tag : info.getTags()) {
                FindIterable<Document> documents = getMovieCollection().find(
                                new Document("tags", new Document("$regex", tag))
                        ).sort(Sorts.descending("score"))
                        .limit(info.getPage() * info.getSize());

                for (Document doc : documents) {
                    MovieTemp movie = documentToMovie(doc);
                    if (movie != null) {
                        allMovies.add(movie);
                    }
                }
            }
        }

        // 按内容搜索
        if (StringUtils.isNotBlank(info.getContent())) {
            FindIterable<Document> documents = getMovieCollection().find(
                            new Document("$or", Arrays.asList(
                                    new Document("name", new Document("$regex", info.getContent())),
                                    new Document("storyline", new Document("$regex", info.getContent())),
                                    new Document("actors", new Document("$regex", info.getContent())),
                                    new Document("directors", new Document("$regex", info.getContent()))
                            ))
                    ).sort(Sorts.descending("score"))
                    .limit(info.getPage() * info.getSize());

            for (Document doc : documents) {
                MovieTemp movie = documentToMovie(doc);
                if (movie != null) {
                    allMovies.add(movie);
                }
            }
        }

        // 去重
        allMovies = allMovies.stream()
                .distinct()
                .collect(Collectors.toList());

        // 分页处理
        int fromIndex = (info.getPage() - 1) * info.getSize();
        int toIndex = Math.min(fromIndex + info.getSize(), allMovies.size());
        List<MovieTemp> movieList = allMovies.subList(fromIndex, toIndex);

        Map<String, Object> result = new HashMap<>(2, 1);
        result.put("total", allMovies.size());
        result.put("movieList", movieList.stream()
                .peek(m -> {
                    if (StringUtils.isEmpty(m.getCover())) {
                        m.setCover(ServerConstant.DefaultImg);
                    }
                })
                .collect(Collectors.toCollection(LinkedList::new)));
        return result;
    }
// ... existing code ...

    public List<MovieTemp> getPersonAttendMovie(String personName) {
        List<MovieTemp> movies = new ArrayList<>();
        FindIterable<Document> documents = getMovieCollection().find(
                new Document("$or", Arrays.asList(
                        new Document("actors", new Document("$regex", personName)),
                        new Document("directors", new Document("$regex", personName))
                ))
        );

        for (Document doc : documents) {
            MovieTemp movie = documentToMovie(doc);
            if (movie != null) {
                if (StringUtils.isEmpty(movie.getCover())) {
                    movie.setCover(ServerConstant.DefaultImg);
                }
                movies.add(movie);
            }
        }

        return movies.stream()
                .collect(Collectors.toCollection(LinkedList::new));
    }

// ... existing code ...
}