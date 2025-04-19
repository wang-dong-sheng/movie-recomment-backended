package pqdong.movie.recommend.mongo.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.mongo.model.domain.MovieMongo;
import pqdong.movie.recommend.mongo.model.domain.Rating;
import pqdong.movie.recommend.mongo.model.recom.Recommendation;
import pqdong.movie.recommend.mongo.model.request.NewRecommendationRequest;
import pqdong.movie.recommend.mongo.utils.Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieMongoService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> movieCollection;
    private MongoCollection<Document> averageMoviesScoreCollection;
    private MongoCollection<Document> rateCollection;

    private MongoCollection<Document> getMovieCollection(){
        if(null == movieCollection)
            movieCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_MOVIE_COLLECTION);
        return movieCollection;
    }

    private MongoCollection<Document> getAverageMoviesScoreCollection(){
        if(null == averageMoviesScoreCollection)
            averageMoviesScoreCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_AVERAGE_MOVIES_SCORE_COLLECTION);
        return averageMoviesScoreCollection;
    }

    private MongoCollection<Document> getRateCollection(){
        if(null == rateCollection)
            rateCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_RATING_COLLECTION);
        return rateCollection;
    }

    public List<MovieMongo> getRecommendeMovies(List<Recommendation> recommendations){
        List<Integer> ids = new ArrayList<>();
        for (Recommendation rec: recommendations) {
            ids.add(rec.getMid());
        }
        return getMovies(ids);
    }

    public List<MovieMongo> getHybirdRecommendeMovies(List<Recommendation> recommendations){
        List<Integer> ids = new ArrayList<>();
        for (Recommendation rec: recommendations) {
            ids.add(rec.getMid());
        }
        return getMovies(ids);
    }

    public List<MovieMongo> getMovies(List<Integer> mids){
        FindIterable<Document> documents = getMovieCollection().find(Filters.in("movieId",mids));
        List<MovieMongo> movieMongos = new ArrayList<>();
        for (Document document: documents) {
            movieMongos.add(documentToMovie(document));
        }
        return movieMongos;
    }

    private MovieMongo documentToMovie(Document document){
        MovieMongo movieMongo = null;
        try{
            movieMongo = objectMapper.readValue(JSON.serialize(document), MovieMongo.class);
            Document score = getAverageMoviesScoreCollection().find(Filters.eq("mid", movieMongo.getMid())).first();
            if(null == score || score.isEmpty())
                movieMongo.setScore(0D);
            else
                movieMongo.setScore((Double) score.get("avg",0D));
        }catch (IOException e) {
            e.printStackTrace();
        }
        return movieMongo;
    }

    private Rating documentToRating(Document document){
        Rating rating = null;
        try{
            rating = objectMapper.readValue(JSON.serialize(document),Rating.class);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return rating;
    }

    public boolean movieExist(int mid){
        return null != findByMID(mid);
    }

    public MovieMongo findByMID(int mid){
        Document document = getMovieCollection().find(new Document("mid",mid)).first();
        if(document == null || document.isEmpty())
            return null;
        return documentToMovie(document);
    }

    public void removeMovie(int mid){
        getMovieCollection().deleteOne(new Document("mid",mid));
    }

    public List<MovieMongo> getMyRateMovies(int uid){
        FindIterable<Document> documents = getRateCollection().find(Filters.eq("uid",uid));
        List<Integer> ids = new ArrayList<>();
        Map<Integer,Double> scores = new HashMap<>();
        for (Document document: documents) {
            Rating rating = documentToRating(document);
            ids.add(rating.getMid());
            scores.put(rating.getMid(),rating.getScore());
        }
        List<MovieMongo> movieMongos = getMovies(ids);
        for (MovieMongo movieMongo : movieMongos) {
            movieMongo.setScore(scores.getOrDefault(movieMongo.getMid(), movieMongo.getScore()));
        }

        return movieMongos;
    }

    public List<MovieMongo> getNewMovies(NewRecommendationRequest request){
        FindIterable<Document> documents = getMovieCollection().find().sort(Sorts.descending("issue")).limit(request.getSum());
        List<MovieMongo> movieMongos = new ArrayList<>();
        for (Document document: documents) {
            movieMongos.add(documentToMovie(document));
        }
        return movieMongos;
    }

}
