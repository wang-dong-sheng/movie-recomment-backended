package pqdong.movie.recommend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pqdong.movie.recommend.annotation.Analysis;
import pqdong.movie.recommend.annotation.AuthCheck;
import pqdong.movie.recommend.annotation.LoginRequired;
import pqdong.movie.recommend.data.constant.UserConstant;
import pqdong.movie.recommend.data.dto.analysis.AnalysisDto;
import pqdong.movie.recommend.data.dto.movie.*;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequest;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequestPage;
import pqdong.movie.recommend.data.dto.rating.RatingVo;
import pqdong.movie.recommend.data.entity.Movie;
import pqdong.movie.recommend.domain.util.ResponseMessage;
import pqdong.movie.recommend.mongo.model.domain.MovieMongo;
import pqdong.movie.recommend.mongo.model.recom.Recommendation;
import pqdong.movie.recommend.mongo.model.request.MovieHybridRecommendationRequest;
import pqdong.movie.recommend.mongo.model.request.MovieRecommendationRequest;
import pqdong.movie.recommend.mongo.model.request.TopGenresRecommendationRequest;
import pqdong.movie.recommend.mongo.model.request.UserRecommendationRequest;
import pqdong.movie.recommend.mongo.service.*;
import pqdong.movie.recommend.newService.MovieNewService;
import pqdong.movie.recommend.temp.MovieTemp;
import pqdong.movie.recommend.temp.RatingTemp;
import pqdong.movie.recommend.temp.UserTemp;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movie")
@Slf4j
public class MovieController {
    @Resource
    private MovieNewService movieNewService;

//    @Resource
//    private MovieService movieService;
//    @Resource
//    private MovieMybatisService movieMybatisService;
//====================================================新加mongo，es相关以及各类推荐相关========================
   @Resource
    private UserMongoService userMongoService;




    @Autowired
    private RecommenderService recommenderService;
    @Autowired
    private MovieMongoService movieMongoService;
    @Resource
    private RatingService ratingService;
//    @Autowired
//    private RatingService ratingService;
//    @Autowired
//    private TagService tagService;

    /**
     * 获取推荐的电影【实时推荐6 + 内容推荐4】
     * 默认推荐4部电影
     * @param username
     * @param
     * @return
     */
    // TODO:   bug 混合推荐结果中，基于内容的推荐，基于MID，而非UID
//    @GetMapping("/guss" )
//    @ResponseBody
//    public ResponseMessage<List<Movie>> getGuessMovies(@RequestParam("username")String username, @RequestParam(value = "num", defaultValue = "4")int num) {
//        UserTemp user = userMongoService.findByUsername(username);
//        //去实时推荐表中找，如果没有那么说明属于冷启动，那么进行基于内容得推荐
//        List<Recommendation> recommendations = recommenderService.getHybridRecommendations(new MovieHybridRecommendationRequest(user.getUserId(),num));
//        //冷启动，基于内容推荐
//        if(recommendations.size()<4){
//            String randomGenres = user.getPrefGenres().get(new Random().nextInt(user.getPrefGenres().size()));
//            recommendations = recommenderService.getTopGenresRecommendations(new TopGenresRecommendationRequest(randomGenres.split(" ")[0],num-recommendations.size()));
//        }
//        List<MovieMongo> hybirdRecommendeMovieMongos = movieMongoService.getHybirdRecommendeMovies(recommendations);
//        List<Movie> movies = hybirdRecommendeMovieMongos.stream().map(movieMongo -> movieMongo.movieMongoToMovie()).collect(Collectors.toList());
//        return ResponseMessage.successMessage(movies);
//    }

    /**
     *基于用户相似矩阵的电影推荐
     * @param username
     * @param model
     * @return
     */
    @GetMapping("/wish" )
    @ResponseBody
    public ResponseMessage<List<Movie>> getWishMovies(@RequestParam("username")String username, @RequestParam(value = "num", defaultValue = "4")int num, Model model) {
        UserTemp user = userMongoService.findByUsername(username);
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new UserRecommendationRequest(user.getUserId(),num));
        if(recommendations.size()==0){
            String randomGenres = user.getPrefGenres().get(new Random().nextInt(user.getPrefGenres().size()));
            recommendations = recommenderService.getTopGenresRecommendations(new TopGenresRecommendationRequest(randomGenres.split(" ")[0],num));
        }
        List<MovieMongo> recommendeMovies = movieMongoService.getRecommendeMovies(recommendations);
        List<Movie> movies = recommendeMovies.stream().map(movieMongo -> movieMongo.movieMongoToMovie()).collect(Collectors.toList());

        return ResponseMessage.successMessage(movies);
    }
    /**
     * 获取电影详细页面相似的电影集合：基于电影相似性的推荐
     * @param
     * @param
     * @return
     */
    @GetMapping("/same")
    @ResponseBody
    public ResponseMessage<List<Movie>> getSameMovie(int movieId, @RequestParam(value = "num", defaultValue = "4")int num) {
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new MovieRecommendationRequest(movieId,num));
        List<MovieMongo> recommendeMovies = movieMongoService.getRecommendeMovies(recommendations);
        List<Movie> movies = recommendeMovies.stream().map(movieMongo -> movieMongo.movieMongoToMovie()).collect(Collectors.toList());

        return ResponseMessage.successMessage(movies);
    }
//    ====================================================新加mongo，es相关以及各类推荐相关========================



//    ============================================基于mysql的相关操作================================

    /**
     * @method getMovieTags 获取电影标签
     */
//    @GetMapping("/tag")
//    public ResponseMessage get() {
////        return ResponseMessage.successMessage(movieNewService.getMovieTags());
//    }

    /**
     * @param key  关键字
     * @param page 当前页数
     * @param size 每页数据量
     * @method allMovie 获取电影列表
     **/
    @GetMapping("/list")
    public ResponseMessage allMovie(
            @RequestParam(required = false, defaultValue = "") String key,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "12") int size) {
        return ResponseMessage.successMessage(movieNewService.getAllMovie(key, page, size));
    }

    /**
     * @param ratingVo 注意这里使用ratingVo单纯是不想重新VO，这里只需要接收userId,movieId，在切面中用
     * @method getMovie 获取电影详情
     **/
    @PostMapping("/info")
    @Analysis
    public ResponseMessage getMovieById(@RequestBody RatingVo ratingVo) {
        return ResponseMessage.successMessage(movieNewService.getMovieById(ratingVo.getMovieId()));
    }

    /**
     * @param personName 演员id
     * @method getPersonMovie 获取演员出演的电影
     **/
    @GetMapping("/person/attend")
    public ResponseMessage getPersonAttendMovie(
            @RequestParam(required = true, defaultValue = "0") String personName) {
        return ResponseMessage.successMessage(movieNewService.getPersonAttendMovie(personName));
    }

    /**
     * @param info 查找条件
     * @method getMovieListByTag 根据标签获取电影列表
     **/
    @PostMapping("/listByTag")
    public ResponseMessage getMovieListByTag(@RequestBody(required = true) MovieSearchDto info) {
        if (info.getTags().isEmpty() && StringUtils.isEmpty(info.getContent())) {
            return ResponseMessage.successMessage(movieNewService.getAllMovie("", info.getPage(), info.getSize()));
        } else {
            return ResponseMessage.successMessage(movieNewService.searchMovies(info));
        }
    }

    /**
     * @method getHighMovie 获取高分电影
     **/
//    @GetMapping("/high")
//    public ResponseMessage<Page<Movie>> getHighMovie() {
//
//        return ResponseMessage.successMessage(movieMybatisService.getHighMovie());
//    }

    /**
     * @param rating 打分
     * @method updateScore 对电影评分
     **/
    @PostMapping("/setRating")
    @LoginRequired
    public ResponseMessage setScore(@RequestBody(required = true) RatingVo rating) {
        return ResponseMessage.successMessage(movieNewService.setScore(rating));
    }

    /**
     * 查看当前用户是否已经对该电影进行打分了
     * 如果打过分数，那么返回对应打分rating数据
     * 如果没打过分数那么返回空，用户前端判断
     * @param ratingUserRequest
     * @method updateScore 对电影评分
     **/
    @PostMapping("/getRating")
    @LoginRequired
    public ResponseMessage<RatingTemp> getScore(@RequestBody(required = true) RatingUserRequest ratingUserRequest) {
        return ResponseMessage.successMessage(movieNewService.getScore(ratingUserRequest));
    }

    /**
     * @method getHighMovie 获取高分电影
     **/
//    @PostMapping("/recommend")
//    public ResponseMessage getRecommendMovie(@RequestBody(required = false) UserEntity user) {
//        return ResponseMessage.successMessage(movieService.getRecommendMovie(user));
//    }

    /**
     * @method 基于用户的推荐
     **/
    @PostMapping("/recommend")
    public ResponseMessage getRecommendMovie(@RequestBody(required = false) MovieRecommendVo movieRecommendVo) {
        return ResponseMessage.successMessage(movieNewService.getRecommendMovie(movieRecommendVo.getUserId(), movieRecommendVo.getType()));
    }

    /*
    删除用户信息
    * */
    @DeleteMapping("/deleteMovies")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> deleteUsers(@RequestBody List<Long> ids) {


        return ResponseMessage.successMessage(movieNewService.deleteMovies(ids));
    }

    /**
     * 按条件查询用户相关信息
     */
    @PostMapping("/filterMovies")
    public ResponseMessage<Page<MovieTemp>> filterMovies(@RequestBody MovieQueryRequest movieQueryRequest) {

        log.info(JSONUtil.toJsonStr(movieQueryRequest));

        return ResponseMessage.successMessage(movieNewService.filterMovies(movieQueryRequest));
    }


    /**
     * 上下架电影
     * @param movieUpVo
     * @return
     */
    @PostMapping("/isUp")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> isUpMovie(@RequestBody MovieUpVo movieUpVo) {

        return ResponseMessage.successMessage(movieNewService.isUpMovie(movieUpVo));
    }

    /**
     * 新增电影
     * @param movie
     * @return
     */
    @PostMapping("/addMovie")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> addMovie(@RequestBody MovieTemp movie) {

        return ResponseMessage.successMessage(movieNewService.addMovie(movie));
    }

    /**
     * 修改电影
     * @param movie
     * @return
     */
    @PostMapping("/updateMovie")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> updateMovie(@RequestBody MovieTemp movie) {

        return ResponseMessage.successMessage(movieNewService.updateMovie(movie));
    }

    /**
     * 查看用户评价过的电影信息
     * @param ratingUserRequest
     * @return
     */
    @PostMapping("/getRatedMovieByUserId")
    @LoginRequired
    public ResponseMessage<Page<MovieTempRating>> getRatedMovieByUserId(@RequestBody(required = true) RatingUserRequestPage ratingUserRequest) {
        return ResponseMessage.successMessage(movieNewService.getRatedMovieByUserId(ratingUserRequest));
    }



}
