package pqdong.movie.recommend.data.dto.movie;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import pqdong.movie.recommend.common.PageRequest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户查询请求
 */

@Data
public class MovieQueryRequest extends PageRequest implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private String actors;

    /**
     *
     */
    private String cover;


    /**
     *
     */
    private String directors;

    /**
     *
     */
    private String genres;

    /**
     *
     */
    private String languages;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String officialSite;

    /**
     *
     */
    private String regions;

    /**
     *
     */
    private Date releaseDate;

    /**
     *
     */
    private Double score;


    /**
     *标签
     */
    private List<String> tags;

    /**
     *
     */
    private Integer votes;

    /**
     *
     */
    private Integer year;

    private Date[] dateRange;
    /**
     * 是否上架
     * 1上架、0：下架
     */
    private Short isUp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}