package pqdong.movie.recommend.data.dto.rating;/**
 * @author Mr.Wang
 * @create 2025-03-01-15:05
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@ClassName RatingUserRequest
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/3/1 15:05
 *@Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingUserRequest {

    private Long userId;
    private Long movieId;
}


