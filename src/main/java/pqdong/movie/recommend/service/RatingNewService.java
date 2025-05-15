//package pqdong.movie.recommend.newService;/**
// * @author Mr.Wang
// * @create 2025-04-19-11:08
// */
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mongodb.MongoClient;
//import com.mongodb.client.FindIterable;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.util.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.bson.Document;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import pqdong.movie.recommend.data.dto.rating.RatingUserRequest;
//import pqdong.movie.recommend.data.dto.rating.RatingVo;
//import pqdong.movie.recommend.kafka.KafkaConstant;
//import pqdong.movie.recommend.kafka.RatingMessageProducer;
//import pqdong.movie.recommend.data.constant.Constant;
//import pqdong.movie.recommend.temp.RatingTemp;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *@ClassName RatingNewService
// *@Description TODO
// *@Author Mr.Wang
// *@Date 2025/4/19 11:08
// *@Version 1.0
// */
//@Service
//@Slf4j
//public class RatingNewService {
//    @Autowired
//    private MongoClient mongoClient;
//    @Resource
//    private RatingMessageProducer ratingMessageProducer;
//    @Autowired
//    private ObjectMapper objectMapper;
//    private MongoCollection<Document> ratingCollection;
//    private MongoCollection<Document> getRatingCollection() {
//        if (null == ratingCollection)
//            ratingCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Rating");
//        return ratingCollection;
//    }
//    public RatingVo setScore(RatingVo ratingVo) {
//        try {
//            // 检查是否已评分
//            Document existingRating = getRatingCollection().find(
//                    new Document("userId", ratingVo.getUserId())
//                            .append("movieId", ratingVo.getMovieId())
//            ).first();
//
//            RatingTemp rating = new RatingTemp();
//            rating.setUserId(ratingVo.getUserId().intValue());
//            rating.setMovieId(ratingVo.getMovieId().intValue());
//            rating.setRating(ratingVo.getRating());
//
//            rating.setTimestamp(String.valueOf(System.currentTimeMillis()));
//
//            if (existingRating != null) {
//                // 更新评分
//                getRatingCollection().updateOne(
//                        new Document("userId", ratingVo.getUserId())
//                                .append("movieId", ratingVo.getMovieId()),
//                        new Document("$set", Document.parse(objectMapper.writeValueAsString(rating)))
//                );
//            } else {
//                // 新增评分
//                getRatingCollection().insertOne(Document.parse(objectMapper.writeValueAsString(rating)));
//            }
//
//            // 更新电影平均分
//            updateMovieAverageScore(ratingVo.getMovieId());
//            //发送消息到kafka
//            ratingMessageProducer.sendRatingMessage(KafkaConstant.RATING_TOPIC,String.valueOf(ratingVo.getUserId()),String.valueOf(ratingVo.getMovieId()),ratingVo.getRating(),(int)System.currentTimeMillis()/100);
//            //将评分放入redis中
//            return ratingVo;
//        } catch (Exception e) {
//            log.error("评分失败", e);
//            return null;
//        }
//    }
//    public RatingTemp getScore(RatingUserRequest ratingUserRequest) {
//        Document document = getRatingCollection().find(
//                new Document("userId", ratingUserRequest.getUserId())
//                        .append("movieId", ratingUserRequest.getMovieId())
//        ).first();
//
//        if (document == null || document.isEmpty()) {
//            return null;
//        }
//        return documentToRating(document);
//    }
//
//    private void updateMovieAverageScore(Long movieId) {
//        List<RatingTemp> ratings = new ArrayList<>();
//        FindIterable<Document> documents = getRatingCollection().find(new Document("movieId", movieId));
//        for (Document doc : documents) {
//            ratings.add(documentToRating(doc));
//        }
//
//        if (!ratings.isEmpty()) {
//            double averageScore = ratings.stream()
//                    .mapToDouble(RatingTemp::getRating)
//                    .average()
//                    .orElse(0.0);
//
//            getMovieCollection().updateOne(
//                    new Document("movieId", movieId),
//                    new Document("$set", new Document("score", averageScore))
//            );
//        }
//    }
//    private RatingTemp documentToRating(Document document) {
//        RatingTemp rating = null;
//        try {
//            rating = objectMapper.readValue(JSON.serialize(document), RatingTemp.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return rating;
//    }
//}
//
//
