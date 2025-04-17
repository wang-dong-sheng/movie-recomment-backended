package pqdong.movie.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import pqdong.movie.recommend.data.entity.Movie;

import java.util.List;

/**
* @author champion
* @description 针对表【movie】的数据库操作Mapper
* @createDate 2025-03-01 13:21:36
* @Entity generator.domain.Movie
*/
public interface MovieMapper extends BaseMapper<Movie> {
    List<Movie> selectTopMovies(int limit);
    List<Movie> selectLimit(int limit);
}




