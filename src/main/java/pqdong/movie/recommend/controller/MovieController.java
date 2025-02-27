package pqdong.movie.recommend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import pqdong.movie.recommend.annotation.AuthCheck;
import pqdong.movie.recommend.annotation.LoginRequired;
import pqdong.movie.recommend.common.PageRequest;
import pqdong.movie.recommend.data.constant.UserConstant;
import pqdong.movie.recommend.data.dto.RatingDto;
import pqdong.movie.recommend.data.dto.movie.MovieQueryRequest;
import pqdong.movie.recommend.data.dto.movie.MovieSearchDto;
import pqdong.movie.recommend.data.dto.movie.MovieUpVo;
import pqdong.movie.recommend.data.entity.Movie;
import pqdong.movie.recommend.data.entity.UserEntity;
import pqdong.movie.recommend.domain.util.ResponseMessage;
import pqdong.movie.recommend.service.jpa.MovieService;
import pqdong.movie.recommend.service.mabatis.MovieMybatisService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/movie")
@Slf4j
public class MovieController {

    @Resource
    private MovieService movieService;
    @Resource
    private MovieMybatisService movieMybatisService;

    /**
     * @method getMovieTags 获取电影标签
     */
    @GetMapping("/tag")
    public ResponseMessage get() {
        return ResponseMessage.successMessage(movieService.getMovieTags());
    }

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
        return ResponseMessage.successMessage(movieService.getAllMovie(key, page, size));
    }

    /**
     * @param movieId 电影id
     * @method getMovie 获取电影详情
     **/
    @GetMapping("/info")
    public ResponseMessage getMovie(
            @RequestParam(required = true, defaultValue = "0") Long movieId) {
        return ResponseMessage.successMessage(movieService.getMovie(movieId));
    }

    /**
     * @param personName 演员id
     * @method getPersonMovie 获取演员出演的电影
     **/
    @GetMapping("/person/attend")
    public ResponseMessage getPersonAttendMovie(
            @RequestParam(required = true, defaultValue = "0") String personName) {
        return ResponseMessage.successMessage(movieService.getPersonAttendMovie(personName));
    }

    /**
     * @param info 查找条件
     * @method getMovieListByTag 根据标签获取电影列表
     **/
    @PostMapping("/listByTag")
    public ResponseMessage getMovieListByTag(@RequestBody(required = true) MovieSearchDto info) {
        if (info.getTags().isEmpty() && StringUtils.isEmpty(info.getContent())) {
            return ResponseMessage.successMessage(movieService.getAllMovie("", info.getPage(), info.getSize()));
        } else {
            return ResponseMessage.successMessage(movieService.searchMovies(info));
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
    @PostMapping("/update")
    @LoginRequired
    public ResponseMessage updateScore(@RequestBody(required = true) RatingDto rating) {
        return ResponseMessage.successMessage(movieService.updateScore(rating));
    }

    /**
     * @method getHighMovie 获取高分电影
     **/
    @PostMapping("/recommend")
    public ResponseMessage getRecommendMovie(@RequestBody(required = false) UserEntity user) {
        return ResponseMessage.successMessage(movieService.getRecommendMovie(user));
    }

    /*
     * 获取所有用户信息
     * */
    @LoginRequired
    @PostMapping("/getAllMovie")
    public ResponseMessage<Page<Movie>> getAllMovie(PageRequest pageRequest) {


        return ResponseMessage.successMessage(movieMybatisService.getAllMovie(pageRequest));
    }


    /*
    删除用户信息
    * */
    @DeleteMapping("/deleteMovies")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> deleteUsers(@RequestBody List<Long> ids) {


        return ResponseMessage.successMessage(movieMybatisService.deleteMovies(ids));
    }

    /**
     * 按条件查询用户相关信息
     */
    @PostMapping("/filterMovies")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Page<Movie>> filterMovies(@RequestBody MovieQueryRequest movieQueryRequest) {

        log.info(JSONUtil.toJsonStr(movieQueryRequest));

        return ResponseMessage.successMessage(movieMybatisService.filterMovies(movieQueryRequest));
    }


    /**
     * 上下架电影
     * @param movieUpVo
     * @return
     */
    @PostMapping("/isUp")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> isUpMovie(@RequestBody MovieUpVo movieUpVo) {

        return ResponseMessage.successMessage(movieMybatisService.isUpMovie(movieUpVo));
    }

    /**
     * 新增电影
     * @param movie
     * @return
     */
    @PostMapping("/addMovie")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> addMovie(@RequestBody Movie movie) {

        return ResponseMessage.successMessage(movieMybatisService.addMovie(movie));
    }

    /**
     * 修改电影
     * @param movie
     * @return
     */
    @PostMapping("/updateMovie")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> updateMovie(@RequestBody Movie movie) {

        return ResponseMessage.successMessage(movieMybatisService.updateMovie(movie));
    }

}
