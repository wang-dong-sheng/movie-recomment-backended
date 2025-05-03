package pqdong.movie.recommend.data.dto.movie;/**
 * @author Mr.Wang
 * @create 2025-05-03-21:15
 */

import lombok.Data;
import pqdong.movie.recommend.temp.MovieTemp;

/**
 *@ClassName MovieTempRating
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/3 21:15
 *@Version 1.0
 */
@Data
public class MovieTempRating extends MovieTemp {
    private Double rating;

}


