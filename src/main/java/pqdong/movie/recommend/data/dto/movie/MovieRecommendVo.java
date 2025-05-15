package pqdong.movie.recommend.data.dto.movie;/**
 * @author Mr.Wang
 * @create 2025-04-06-16:01
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pqdong.movie.recommend.data.enums.MovieRecommentEnum;

/**
 *@ClassName MovieRecommentVo
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/4/6 16:01
 *@Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieRecommendVo  {
    MovieRecommentEnum type;
    Long userId;
}


