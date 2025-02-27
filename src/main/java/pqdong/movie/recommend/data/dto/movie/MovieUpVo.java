package pqdong.movie.recommend.data.dto.movie;/**
 * @author Mr.Wang
 * @create 2025-02-27-13:41
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@ClassName MovieUpVo
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/2/27 13:41
 *@Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieUpVo {
    /**
     * 上下架标志
     * 1表示执行上架方法
     * 0表示执行下架方法
     */
    private Short tag;

    private Long movieId;
}


