package pqdong.movie.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import pqdong.movie.recommend.data.entity.User;

import java.util.List;

/**
* @author champion
* @description 针对表【user】的数据库操作Mapper
* @createDate 2025-03-01 13:21:40
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {
    List<User> selectLimitUser(int num);
}




