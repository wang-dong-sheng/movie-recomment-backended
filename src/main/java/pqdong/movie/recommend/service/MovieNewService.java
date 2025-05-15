package pqdong.movie.recommend.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
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
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.common.ErrorCode;
import pqdong.movie.recommend.constant.ServerConstant;
import pqdong.movie.recommend.data.dto.comments.CommentsDto;
import pqdong.movie.recommend.data.dto.movie.MovieQueryRequest;
import pqdong.movie.recommend.data.dto.movie.MovieSearchDto;
import pqdong.movie.recommend.data.dto.movie.MovieTempRating;
import pqdong.movie.recommend.data.dto.movie.MovieUpVo;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequest;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequestPage;
import pqdong.movie.recommend.data.dto.rating.RatingVo;
import pqdong.movie.recommend.data.entity.CommentsTemp;
import pqdong.movie.recommend.data.entity.UserTemp;
import pqdong.movie.recommend.exception.ThrowUtils;
import pqdong.movie.recommend.kafka.KafkaConstant;
import pqdong.movie.recommend.kafka.RatingMessageProducer;
import pqdong.movie.recommend.data.dto.Recommendation.Recommendation;
import pqdong.movie.recommend.constant.Constant;
import pqdong.movie.recommend.redis.RedisApi;
import pqdong.movie.recommend.data.entity.MovieTemp;
import pqdong.movie.recommend.data.entity.RatingTemp;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieNewService {
    @Autowired
    private Jedis jedis;
    @Resource
    private RatingMessageProducer ratingMessageProducer;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;
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

    //    private UserTemp getMovieTags()
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

    //    public Page<MovieTemp> filterMovies(MovieQueryRequest movieQueryRequest) {
//        String movieName = movieQueryRequest.getName();
//        Date[] dateRange = movieQueryRequest.getDateRange();
//        long current = movieQueryRequest.getCurrent();
//        long pageSize = movieQueryRequest.getPageSize();
//
//        // 构建查询条件
//        List<Bson> conditions = new ArrayList<>();
//
//        if (StringUtils.isNotBlank(movieName)) {
//            conditions.add(Filters.eq("name", movieName));
//        }
//
//        if (dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null) {
//            DateTime startDate = DateUtil.date(dateRange[0]);
//            DateTime endDate = DateUtil.date(dateRange[1]);
//            //获取发行年份
//            conditions.add(Filters.and(
//                    Filters.gte("year", startDate.year()),
//                    Filters.lte("year", endDate.year())
//            ));
//        }
//
//        Bson query = conditions.isEmpty() ? new Document() : Filters.and(conditions);
//
//        // 执行查询
//        FindIterable<Document> documents = getMovieCollection().find(query)
//                .skip((int) ((current - 1) * pageSize))
//                .limit((int) pageSize);
//
//        // 获取总记录数
//        long total = getMovieCollection().count(query);
//        ArrayList<MovieTemp> movies = new ArrayList<>();
//        for (Document document : documents) {
//            MovieTemp movieTemp = documentToMovie(document);
//            movies.add(movieTemp);
//        }
//        // 转换结果
//        // 构建分页结果
//        Page<MovieTemp> page = new Page<>(current, pageSize);
//        page.setTotal(total);
//        page.setRecords(movies);
//        return page;
//    }
    public Page<MovieTemp> filterMovies(MovieQueryRequest movieQueryRequest) {
        Page<MovieTemp> page = new Page<>(movieQueryRequest.getCurrent(), movieQueryRequest.getPageSize());
        List<MovieTemp> movies = new ArrayList<>();
        Date[] dateRange = movieQueryRequest.getDateRange();
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
                if (dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null) {
                    DateTime startDate = DateUtil.date(dateRange[0]);
                    DateTime endDate = DateUtil.date(dateRange[1]);
                    //获取发行年份
                    if (movie.getYear() < startDate.year() || movie.getYear() > endDate.year()) continue;
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

            rating.setTimestamp((int) System.currentTimeMillis() / 1000);

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
            //发送消息到kafka
            ratingMessageProducer.sendRatingMessage(KafkaConstant.RATING_TOPIC, String.valueOf(ratingVo.getUserId()), String.valueOf(ratingVo.getMovieId()), ratingVo.getRating(), (int) System.currentTimeMillis() / 1000);

            //更新redis评分
            updateRedis(ratingVo);
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

    public List<MovieTemp> getHybirdRecommendeMovies(List<Recommendation> recommendations) {
        List<Integer> ids = new ArrayList<>();
        for (Recommendation rec : recommendations) {
            ids.add(rec.getMid());
        }
        return getMovies(ids);
    }

    public List<MovieTemp> getMovies(List<Integer> mids) {
        FindIterable<Document> documents = getMovieCollection().find(Filters.in("movieId", mids));
        List<MovieTemp> movieMongos = new ArrayList<>();
        for (Document document : documents) {
            movieMongos.add(documentToMovie(document));
        }
        return movieMongos;
    }

    public List<MovieTemp> getRecommendeMovies(List<Recommendation> recommendations) {
        List<Integer> ids = new ArrayList<>();
        for (Recommendation rec : recommendations) {
            ids.add(rec.getMid());
        }
        return getMovies(ids);
    }

    //评分集合重始终保留用户评分过的REDIS_MOVIE_RATING_QUEUE_SIZE条评分
    private void updateRedis(RatingVo rating) {
        if (jedis.exists("userId:" + rating.getUserId()) && jedis.llen("userId:" + rating.getUserId()) >= Constant.REDIS_MOVIE_RATING_QUEUE_SIZE) {
            jedis.rpop("userId:" + rating.getUserId());
        }
        jedis.lpush("userId:" + rating.getUserId(), rating.getMovieId() + ":" + rating.getRating());
    }

    public Page<MovieTempRating> getRatedMovieByUserId(RatingUserRequestPage ratingUserRequestPage) {
        Integer userId = ratingUserRequestPage.getUserId();
        Page<MovieTempRating> page = new Page<>(ratingUserRequestPage.getCurrent(), ratingUserRequestPage.getPageSize());

        List<RatingTemp> userRatings = this.getUserRatings(userId.longValue());
        List<Integer> movieIdList = userRatings.stream().map(RatingTemp::getMovieId).collect(Collectors.toList());
        List<MovieTemp> movies = this.getMovies(movieIdList);
        //查询分数
        List<MovieTempRating> movieTempRatingList = movies.stream().map(movie -> {
            MovieTempRating movieTempRating = new MovieTempRating();
            BeanUtil.copyProperties(movie, movieTempRating);
            RatingUserRequest ratingUserRequest = new RatingUserRequest(userId.longValue(), movie.getMovieId().longValue());
            RatingTemp rating = getScore(ratingUserRequest);
            movieTempRating.setRating(rating.getRating());
            return movieTempRating;
        }).collect(Collectors.toList());

        page.setTotal(movieTempRatingList.size());
        int fromIndex = (int) ((page.getCurrent() - 1) * page.getSize());
        int toIndex = Math.min(fromIndex + (int) page.getSize(), movies.size());
        page.setRecords(movieTempRatingList.subList(fromIndex, toIndex));
        return page;

    }
}