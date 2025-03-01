package pqdong.movie.recommend.service.mabatis.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.common.ErrorCode;
import pqdong.movie.recommend.common.PageRequest;
import pqdong.movie.recommend.data.constant.UserConstant;
import pqdong.movie.recommend.data.dto.user.UserQueryRequest;
import pqdong.movie.recommend.data.entity.User;
import pqdong.movie.recommend.exception.ThrowUtils;
import pqdong.movie.recommend.mapper.UserMapper;
import pqdong.movie.recommend.service.mabatis.UserMybatisService;
import pqdong.movie.recommend.utils.ThreadLocalUtils;

import java.util.Date;
import java.util.List;

/**
 * userService
 *
 * @author pqdong
 * @since 2020/03/03
 */
@Slf4j
@Service

public class UserMybatisServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserMybatisService {

    @Override
    public Page<User> getAllUser(PageRequest pageRequest) {
        long pageSize = pageRequest.getPageSize();
        long current = pageRequest.getCurrent();
        Page<User> userPage = new Page<>(current, pageSize);
        Page<User> resPage = this.page(userPage);
        return resPage;
    }

    @Override
    public Boolean deleteUsers(List<Long> ids) {
        //1. todo 先不搞了，赶进度检验id是否存在
        //不能删除自己
        Long currentId = ThreadLocalUtils.getCurrentId();

        ThrowUtils.throwIf(ids.contains(currentId), ErrorCode.NO_AUTH_ERROR, "不能删除自己");

        List<User> users = this.listByIds(ids);
        users.forEach((user)->{
            String userrole = user.getUserrole();
            ThrowUtils.throwIf(UserConstant.ADMIN_ROLE.equals(userrole), ErrorCode.NO_AUTH_ERROR, "不能删除管理员");
        });
        //2.删除
        boolean b = this.removeBatchByIds(ids);
        if (b) {
            return true;
        }

        return false;
    }

    @Override
    public Page<User> filterUsers(UserQueryRequest userQueryRequest) {
        String userNickname = userQueryRequest.getUserNickname();
        Long userId = userQueryRequest.getUserId();
        Date[] dateRange = userQueryRequest.getDateRange();
        Page<User> userPage = new Page<>();
        userPage.setCurrent(userQueryRequest.getCurrent());
        userPage.setSize(userQueryRequest.getPageSize());
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(userNickname), "user_nickname", userNickname);
        wrapper.eq(userId != null, "id", userId);
        boolean isRange = dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null;
        if (isRange) {
            wrapper.between(isRange, "DATE(create_time)", dateRange[0], dateRange[1]);
        }
        Page<User> page = this.page(userPage, wrapper);
        return page;
    }
}
