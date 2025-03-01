package pqdong.movie.recommend.service.mabatis.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.common.ErrorCode;
import pqdong.movie.recommend.data.constant.MovieConstant;
import pqdong.movie.recommend.data.dto.movie.MovieQueryRequest;
import pqdong.movie.recommend.data.dto.movie.MovieUpVo;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequest;
import pqdong.movie.recommend.data.dto.rating.RatingVo;
import pqdong.movie.recommend.data.entity.Movie;
import pqdong.movie.recommend.data.entity.Rating;
import pqdong.movie.recommend.data.entity.User;
import pqdong.movie.recommend.exception.ThrowUtils;
import pqdong.movie.recommend.mapper.MovieMapper;
import pqdong.movie.recommend.service.mabatis.MovieMybatisService;
import pqdong.movie.recommend.service.mabatis.RatingMybatisService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author champion
 * @description 针对表【movie】的数据库操作Service实现
 * @createDate 2025-03-01 13:38:32
 */
@Service
public class MovieMybatisServiceImpl extends ServiceImpl<MovieMapper, Movie>
        implements MovieMybatisService {
    @Resource
    private RatingMybatisService ratingMybatisService;

    @Override
    public Boolean deleteMovies(List<Long> ids) {
        boolean b = this.removeByIds(ids);
        return b;
    }

    @Override
    public Page<Movie> filterMovies(MovieQueryRequest movieQueryRequest) {
        Long id = movieQueryRequest.getId();
        String name = movieQueryRequest.getName();
        List<String> tags = movieQueryRequest.getTags();
        Date[] dateRange = movieQueryRequest.getRangeDate();
        long current = movieQueryRequest.getCurrent();
        long pageSize = movieQueryRequest.getPageSize();
        Page<Movie> moviePage = new Page<>();
        moviePage.setCurrent(current);
        moviePage.setSize(pageSize);
        QueryWrapper<Movie> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(name), "name", name);
        wrapper.eq(id != null, "id", id);
        boolean isRange = dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null;
        if (isRange) {
            wrapper.between(isRange, "DATE(create_time)", dateRange[0], dateRange[1]);
        }
        Page<Movie> resPage = this.page(moviePage, wrapper);
        if (tags != null && !tags.isEmpty()) {
            List<Movie> records = resPage.getRecords();
            ArrayList<Movie> movies = new ArrayList<>();
            for (Movie record : records) {
                for (String tag : tags) {
                    if (!record.getTags().contains(tag)) {
                        break;
                    }
                }
                movies.add(record);
                resPage.setTotal(movies.size());
                resPage.setRecords(movies);
            }
        }


        return resPage;
    }

    @Override
    public Boolean isUpMovie(MovieUpVo movieUpVo) {
        Short tag = movieUpVo.getTag();
        Long movieId = movieUpVo.getMovieId();
        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setIsUp(tag);
        boolean b = this.updateById(movie);
        return b;
    }

    @Override
    public Boolean addMovie(Movie movie) {
        //1.查看视频是否重复，电影名不重复就好
        String name = movie.getName();
        QueryWrapper<Movie> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(name), "name", name);
        List<Movie> list = this.list(wrapper);
        ThrowUtils.throwIf(list != null && list.isEmpty(), ErrorCode.OPERATION_ERROR, "当前电电影已存在");
        boolean save = this.save(movie);
        return save;
    }

    @Override
    public Boolean updateMovie(Movie movie) {
        boolean b = this.updateById(movie);
        return b;
    }

    @Override
    public Movie getMovieById(Long movieId) {
        Movie movie = this.getById(movieId);
        return movie;
    }

    @Override
    public RatingVo setScore(RatingVo ratingVo) {
        Long movieId = ratingVo.getMovieId();
        Long userId = ratingVo.getUserId();
        Float rating = ratingVo.getRating();
        //1.查询当前用户是否已经打过分数了
        QueryWrapper<Rating> wrapper = new QueryWrapper<>();
        wrapper.eq(movieId != null, "movie_id", movieId);
        wrapper.eq(userId != null, "user_id", userId);
        Rating ratingOne = ratingMybatisService.getOne(wrapper);
        Rating updateOrSaveRating = new Rating();
        updateOrSaveRating.setMovieId(movieId);
        updateOrSaveRating.setRating(rating);
        updateOrSaveRating.setUserId(userId);
        if (ratingOne != null) {
            //2.如果已经打分，那么执行修改分数逻辑

            ratingMybatisService.updateById(updateOrSaveRating);
        } else {
            //3.如果没打过分数，那么新建保存分数，保证一个用户和电影频分之间为一对一关系
            ratingMybatisService.save(updateOrSaveRating);
        }

        return ratingVo;

    }

    @Override
    public Rating getScore(RatingUserRequest ratingUserRequest) {
        Long movieId = ratingUserRequest.getMovieId();
        Long userId = ratingUserRequest.getUserId();
        QueryWrapper<Rating> wrapper = new QueryWrapper<>();
        wrapper.eq(movieId != null, "movie_id", movieId);
        wrapper.eq(userId != null, "user_id", userId);
        Rating ratingOne = ratingMybatisService.getOne(wrapper);
        return ratingOne;
    }
}




