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
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String password;

    /**
     * 
     */
    private String userAvatar;

    /**
     * 
     */
    private String userMd;

    /**
     * 
     */
    private String userNickname;

    /**
     * 
     */
    private String userTags;

    /**
     * 
     */
    private String phone;

    /**
     * 
     */
    private String motto;

    /**
     * 
     */
    private String sex;

    /**
     * 
     */
    private String userMd5;

    /**
     * 用户角色
     */
    private String userrole;

    /**
     * 更新时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}