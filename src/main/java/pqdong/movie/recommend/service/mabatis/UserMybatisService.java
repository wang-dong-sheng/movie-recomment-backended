package pqdong.movie.recommend.service.mabatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import pqdong.movie.recommend.common.PageRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import pqdong.movie.recommend.data.dto.user.UserQueryRequest;
import pqdong.movie.recommend.data.entity.User;

import java.util.List;

/**
* @author champion
* @description 针对表【user】的数据库操作Service
* @createDate 2025-02-26 10:34:26
*/
public interface UserMybatisService extends IService<User> {

    /**
     * 分页获取用户信息
     * @param pageRequest
     * @return
     */
    Page<User> getAllUser(PageRequest pageRequest);

    /**
     * 删除用户
     * @param ids
     * @return
     */
    Boolean deleteUsers(List<Long> ids);

    /**
     * 按条件查询用户
     * @param userQueryRequest
     * @return
     */
    Page<User> filterUsers(UserQueryRequest userQueryRequest);
}
