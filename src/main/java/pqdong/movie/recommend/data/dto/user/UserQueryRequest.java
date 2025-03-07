package pqdong.movie.recommend.data.dto.user;

import pqdong.movie.recommend.common.PageRequest;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询请求
 */

@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

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
    private String userRole;

    /**
     * 更新时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private Date[] dateRange;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}