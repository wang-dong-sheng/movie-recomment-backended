package pqdong.movie.recommend.data.dto.movie;/**
 * @author Mr.Wang
 * @create 2025-04-18-11:27
 */

import lombok.Data;
import pqdong.movie.recommend.enums.MovieRecommentEnum;

/**
 *@ClassName RecommendVo
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/4/18 11:27
 *@Version 1.0
 */
@Data
public class RecommendVo {
    MovieRecommentEnum type;
    private String username;
    private Integer userId;
    private Integer movieId;
//    要推荐的数量
    private Integer num=4;
}


