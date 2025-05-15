package pqdong.movie.recommend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import pqdong.movie.recommend.annotation.Analysis;
import pqdong.movie.recommend.annotation.AuthCheck;
import pqdong.movie.recommend.annotation.LoginRequired;
import pqdong.movie.recommend.constant.UserConstant;
import pqdong.movie.recommend.data.dto.movie.MovieQueryRequest;
import pqdong.movie.recommend.data.dto.movie.MovieSearchDto;
import pqdong.movie.recommend.data.dto.movie.MovieTempRating;
import pqdong.movie.recommend.data.dto.movie.MovieUpVo;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequest;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequestPage;
import pqdong.movie.recommend.data.dto.rating.RatingVo;
import pqdong.movie.recommend.common.ResponseMessage;
import pqdong.movie.recommend.service.MovieNewService;
import pqdong.movie.recommend.data.entity.MovieTemp;
import pqdong.movie.recommend.data.entity.RatingTemp;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/movie")
@Slf4j
public class MovieController {
    @Resource
    private MovieNewService movieNewService;


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

    /*
    删除用户信息
    * */
    @DeleteMapping("/deleteMovies")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> deleteUsers(@RequestBody List<Long> ids) {


        return ResponseMessage.successMessage(movieNewService.deleteMovies(ids));
    }

    /**
     * 按条件查询电影相关信息
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
