package pqdong.movie.recommend.service.mabatis;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import pqdong.movie.recommend.data.dto.movie.MovieQueryRequest;
import pqdong.movie.recommend.data.dto.movie.MovieUpVo;
import pqdong.movie.recommend.data.dto.rating.RatingUserRequest;
import pqdong.movie.recommend.data.dto.rating.RatingVo;
import pqdong.movie.recommend.data.entity.Movie;
import pqdong.movie.recommend.data.entity.Rating;
import pqdong.movie.recommend.data.entity.User;

import java.util.List;

/**
* @author champion
* @description 针对表【movie】的数据库操作Service
* @createDate 2025-03-01 13:38:32
*/
public interface MovieMybatisService extends IService<Movie> {

    /**
     * 按ids删除电影
     * @param ids
     * @return
     */
    Boolean deleteMovies(List<Long> ids);

    /**
     * 按条件筛选电影
     * @param movieQueryRequest
     * @return
     */

    Page<Movie> filterMovies(MovieQueryRequest movieQueryRequest);

    /**
     * 上架下映视频
     * 1：上映
     * 0：下架
     * @param movieUpVo
     * @return
     */

    Boolean isUpMovie(MovieUpVo movieUpVo);

    /**
     * 添加视频
     * @param movie
     * @return
     */

    Boolean addMovie(Movie movie);

    /**
     * 更新视频
     *
     * @param movie
     * @return
     */
    Boolean updateMovie(Movie movie);

    /**
     * 根据id查询
     * @param movieId
     * @return
     */
    Movie getMovieById(Long movieId);

    /**
     * 为视频打分
     * @param rating
     * @return
     */
    RatingVo setScore(RatingVo rating);

    /**
     * 查看当前用户是否已经打过份数了
     * @param ratingUserRequest
     * @return
     */
    Rating getScore(RatingUserRequest ratingUserRequest);

    /**
     * 推荐电影
     * @param user
     * @return
     */
    List<Movie> getRecommendMovie(User user);
}
