package pqdong.movie.recommend.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName comments
 */
@TableName(value ="comments")
@Data
public class Comments implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long movieId;

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
     * 
     */
    private Long userId;

    /**
     * 打分
     */
    private Integer rating;

    private String userMd;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}