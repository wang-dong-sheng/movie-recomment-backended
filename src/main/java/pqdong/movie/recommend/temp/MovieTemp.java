package pqdong.movie.recommend.temp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MovieTemp implements Serializable {
    @JsonIgnore
    private String _id;
    /**
     * 
     */
    private Integer movieId;

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


    /**
     * 上架下架（1：上架，2：下架）
     */
    private Short isUp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}