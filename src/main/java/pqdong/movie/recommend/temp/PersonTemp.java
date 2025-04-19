package pqdong.movie.recommend.temp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName person
 */
@TableName(value ="person")
@Data
public class PersonTemp implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long personId;

    /**
     * 
     */
    private String biography;

    /**
     * 
     */
    private String birth;

    /**
     * 
     */
    private String birthPlace;

    /**
     * 演员别称
     */
    private String otherName;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String nameEn;

    /**
     * 星座
     */
    private String constellation;

    /**
     * 
     */
    private String profession;

    /**
     * 
     */
    private String sex;

    private String avatar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}