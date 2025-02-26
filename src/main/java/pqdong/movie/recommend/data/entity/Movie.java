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
 * @TableName movie
 */
@TableName(value ="movie")
@Data
public class Movie implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String actorIds;

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
    private String directorIds;

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
    private Integer mins;

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
     * 
     */
    private String storyline;

    /**
     * 
     */
    private String tags;

    /**
     * 
     */
    private Integer votes;

    /**
     * 
     */
    private Integer year;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}