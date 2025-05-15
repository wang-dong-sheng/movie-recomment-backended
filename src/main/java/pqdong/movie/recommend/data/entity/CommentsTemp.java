package pqdong.movie.recommend.data.entity;/**
 * @author Mr.Wang
 * @create 2025-04-17-17:42
 */

import lombok.Data;

/**
 *@ClassName CommentsTemp
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/4/17 17:42
 *@Version 1.0
 */
@Data
public class CommentsTemp {
//    @JsonIgnore
    private String _id;

    /**
     *
     */
    private Integer movieId;

    /**
     *
     */
    private String content;

    /**
     *
     */
    private Integer votes;

    /**
     *
     */
    private String commentTime;

    /**
     *
     */
    private Integer userId;

    /**
     * 打分
     */
    private Double rating;

}


