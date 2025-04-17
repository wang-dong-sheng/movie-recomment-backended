package pqdong.movie.recommend.temp;/**
 * @author Mr.Wang
 * @create 2025-04-17-10:49
 */

import lombok.Data;

import java.util.Date;

/**
 *@ClassName RatingTemp
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/4/17 10:49
 *@Version 1.0
 */
@Data
public class RatingTemp {
    private Integer userId;
    private Integer movieId;
    private Double rating;
    private Date ratingTime;
}


