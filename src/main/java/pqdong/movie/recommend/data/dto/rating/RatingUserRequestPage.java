package pqdong.movie.recommend.data.dto.rating;/**
 * @author Mr.Wang
 * @create 2025-03-01-15:05
 */

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pqdong.movie.recommend.common.PageRequest;

import java.io.Serializable;

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
public class RatingUserRequestPage extends PageRequest implements Serializable {

    private Integer userId;
    private Integer movieId;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}


