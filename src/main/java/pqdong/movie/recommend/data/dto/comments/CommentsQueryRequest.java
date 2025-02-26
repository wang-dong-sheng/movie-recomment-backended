package pqdong.movie.recommend.data.dto.comments;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import pqdong.movie.recommend.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询请求
 */

@Data
public class CommentsQueryRequest extends PageRequest implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long userId;

    /**
     *
     */
    private String userName;

    /**
     *
     */
    private String useravatar;

    /**
     *
     */
    private Integer movieId;

    /**
     *
     */
    private String movieName;

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
    private Date commentTime;

    /**
     * 日期过滤
     */
    private Date[] dateRange;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}