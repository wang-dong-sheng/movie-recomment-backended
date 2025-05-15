package pqdong.movie.recommend.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @TableName user
 */
@TableName(value = "user")
@Data
public class UserTemp implements Serializable {


    private Integer userId;

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
     * 用户角色
     */
    private String userRole;

    /**
     * 更新时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    private List<String> prefGenres = new ArrayList<>();
    //    是否为新用户
    private boolean first;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}