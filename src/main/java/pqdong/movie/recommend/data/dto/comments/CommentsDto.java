package pqdong.movie.recommend.data.dto.comments;/**
 * @author Mr.Wang
 * @create 2025-04-17-19:28
 */

import lombok.Data;
import pqdong.movie.recommend.data.entity.CommentsTemp;

/**
 *@ClassName CommentsDto
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/4/17 19:28
 *@Version 1.0
 */
@Data
public class CommentsDto extends CommentsTemp {
    private String userName;
    private String movieName;
}


