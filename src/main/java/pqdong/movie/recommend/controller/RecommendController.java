package pqdong.movie.recommend.controller;/**
 * @author Mr.Wang
 * @create 2025-04-18-10:42
 */

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pqdong.movie.recommend.data.dto.movie.MovieRecommendVo;
import pqdong.movie.recommend.data.dto.movie.RecommendVo;
import pqdong.movie.recommend.domain.util.ResponseMessage;
import pqdong.movie.recommend.mongo.model.domain.User;
import pqdong.movie.recommend.mongo.model.recom.Recommendation;
import pqdong.movie.recommend.mongo.model.request.*;
import pqdong.movie.recommend.mongo.service.MovieMongoService;
import pqdong.movie.recommend.mongo.service.RecommenderService;
import pqdong.movie.recommend.newService.MovieNewService;
import pqdong.movie.recommend.newService.UserNewService;
import pqdong.movie.recommend.temp.MovieTemp;
import pqdong.movie.recommend.temp.UserTemp;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * @ClassName RecommendController
 * @Description TODO
 * @Author Mr.Wang
 * @Date 2025/4/18 10:42
 * @Version 1.0
 */
@RestController()
@RequestMapping("/movie/recommend")
public class RecommendController {

    @Resource
    private RecommenderService recommenderService;

    @Resource
    private MovieMongoService movieMongoService;
    @Resource
    private MovieNewService movieNewService;

    @Resource
    private UserNewService userNewService;

    /**
     * 获取推荐的电影【实时推荐6 + 内容推荐4】
     *
     * @param
     * @param
     * @return
     */
    // TODO:   bug 混合推荐结果中，基于内容的推荐，基于MID，而非UID
    @PostMapping( "/hybrid")
    public ResponseMessage getGuessMovies(@RequestBody(required = false) RecommendVo recommendVo, @RequestParam(defaultValue = "4", value = "num") int num) {
        UserTemp user = userNewService.findByUsername(recommendVo.getUsername());
        List<Recommendation> recommendations = recommenderService.getHybridRecommendations(recommendVo);
        if (recommendations.size() == 0) {
            String randomGenres = user.getPrefGenres().get(new Random().nextInt(user.getPrefGenres().size()));
            recommendations = recommenderService.getTopGenresRecommendations(new TopGenresRecommendationRequest(randomGenres.split(" ")[0], num));
        }

        return ResponseMessage.successMessage(movieNewService.getHybirdRecommendeMovies(recommendations));
    }
    /**
     * 实时推荐
     *
     * @param
     * @param
     * @return
     */
    @PostMapping( "/currentTime")
    public ResponseMessage getcurrentTimeMovies(@RequestBody(required = false) RecommendVo recommendVo, @RequestParam(defaultValue = "4", value = "num") int num) {
        UserTemp user = userNewService.findByUsername(recommendVo.getUsername());
        List<Recommendation> recommendations = recommenderService.findStreamRecs(recommendVo.getUserId(),recommendVo.getNum());
        if (recommendations.size() == 0) {
            String randomGenres = user.getPrefGenres().get(new Random().nextInt(user.getPrefGenres().size()));
            recommendations = recommenderService.getTopGenresRecommendations(new TopGenresRecommendationRequest(randomGenres.split(" ")[0], num));
        }

        return ResponseMessage.successMessage(movieNewService.getHybirdRecommendeMovies(recommendations));
    }

    /**
     * 基于用户相似矩阵的电影推荐
     *
     * @return
     */
    @PostMapping("/wish")
    @ResponseBody
    public ResponseMessage<List<MovieTemp>> getWishMovies(@RequestBody(required = false) RecommendVo recommendVo, @RequestParam(defaultValue = "4", value = "num") int num) {
        UserTemp user = userNewService.findByUsername(recommendVo.getUsername());
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new UserRecommendationRequest(user.getUserId(), num));
        if (recommendations.size() == 0) {
            String randomGenres = user.getPrefGenres().get(new Random().nextInt(user.getPrefGenres().size()));
            recommendations = recommenderService.getTopGenresRecommendations(new TopGenresRecommendationRequest(randomGenres.split(" ")[0], num));
        }
        List<MovieTemp> recommendeMovies = movieNewService.getRecommendeMovies(recommendations);
        return ResponseMessage.successMessage(recommendeMovies);
    }

    /**
     * 获取电影详细页面相似的电影集合：基于电影相似性的推荐
     *
     * @return
     */
    @PostMapping("/baseMovie")
    public ResponseMessage<List<MovieTemp>> getSameMovie(@RequestBody(required = false) RecommendVo recommendVo, @RequestParam(defaultValue = "4", value = "num") int num) {
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new MovieRecommendationRequest(recommendVo.getMovieId(), num));
        List<MovieTemp> recommendeMovies = movieNewService.getRecommendeMovies(recommendations);
        return ResponseMessage.successMessage(recommendeMovies);
    }

    /**
     * 获取最近热门热门推荐：按时间来筛选最近评分数量最多的
     *
     * @return
     */
    @PostMapping( "/hot")
    public ResponseMessage<List<MovieTemp>>  getHotMovies(@RequestParam(defaultValue = "4", value = "num") int num) {
        List<Recommendation> recommendations = recommenderService.getHotRecommendations(new HotRecommendationRequest(num));
        List<MovieTemp> recommendeMovies = movieNewService.getRecommendeMovies(recommendations);
        return ResponseMessage.successMessage(recommendeMovies);
    }

    /**
     * 获取投票最多的电影：所有评分数量的最多
     *
     * @return
     */
    @PostMapping("/rateMore")
    public ResponseMessage<List<MovieTemp>> getRateMoreMovies(@RequestParam(defaultValue = "4", value = "num") int num) {
        List<Recommendation> recommendations = recommenderService.getRateMoreRecommendations(new RateMoreRecommendationRequest(num));
        List<MovieTemp> recommendeMovies = movieNewService.getRecommendeMovies(recommendations);
        return ResponseMessage.successMessage(recommendeMovies);
    }

    /**
     * 获取新添加的电影
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/new", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getNewMovies(@RequestParam("num") int num, Model model) {
        model.addAttribute("success", true);
        model.addAttribute("movies", movieMongoService.getNewMovies(new NewRecommendationRequest(num)));
        return model;
    }

    /**
     * 获取电影详细页面相似的电影集合：基于电影相似性的推荐
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/same/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSameMovie(@PathVariable("id") int id, @RequestParam("num") int num, Model model) {
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new MovieRecommendationRequest(id, num));
        model.addAttribute("success", true);
        model.addAttribute("movies", movieMongoService.getRecommendeMovies(recommendations));
        return model;
    }

}


