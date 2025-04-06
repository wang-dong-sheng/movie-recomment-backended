package pqdong.movie.recommend.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName rating
 */
@TableName(value ="rating_test")
@Data
public class Rating implements Serializable {
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
    private Double rating;

    /**
     * 
     */
    private Date ratingTime;

    /**
     * 
     */
    private Long userId;
    private String userMd;

    /**
     * 
     */
    private Date time;

    /**
     * 
     */
    private Long ratingId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}