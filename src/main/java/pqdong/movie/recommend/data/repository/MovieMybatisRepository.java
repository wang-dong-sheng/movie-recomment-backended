package pqdong.movie.recommend.data.repository;/**
 * @author Mr.Wang
 * @create 2025-03-01-18:18
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Repository;
import pqdong.movie.recommend.data.entity.Movie;
import pqdong.movie.recommend.data.entity.Rating;
import pqdong.movie.recommend.data.entity.User;
import pqdong.movie.recommend.mapper.MovieMapper;
import pqdong.movie.recommend.service.mabatis.MovieMybatisService;
import pqdong.movie.recommend.service.mabatis.RatingMybatisService;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 *@ClassName MovieMybatisRepository
 *@Description
 * 由于推荐需要多次和数据交互，并且逻辑较长，
 * 我们将数据部分全部封装，方法中只管调用api来获取数据，
 * 及数据的处理单独提炼出来
 * 及repository层来处理
 *@Author Mr.Wang
 *@Date 2025/3/1 18:18
 *@Version 1.0
 */
@Repository
public class MovieMybatisRepository {

    @Resource
    private RatingMybatisService ratingMybatisService;

    @Resource
    private MovieMapper movieMapper;

    public List<Rating> getRatingByUser(User user){
        QueryWrapper<Rating> wrapper = new QueryWrapper<>();
        wrapper.eq(user.getId()!=null,"user_id",user.getId());
        List<Rating> list = ratingMybatisService.list(wrapper);
        return list;
    }



    /**
     * 获取几部高分电影
     * @param num
     * @return
     */
    public List<Movie> getHightMovie(int num){
        List<Movie> movies = movieMapper.selectTopMovies(num);
        return movies;
    }

}


