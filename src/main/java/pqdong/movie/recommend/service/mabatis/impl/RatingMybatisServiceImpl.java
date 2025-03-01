package pqdong.movie.recommend.service.mabatis.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import pqdong.movie.recommend.data.entity.Rating;
import pqdong.movie.recommend.service.mabatis.RatingMybatisService;
import pqdong.movie.recommend.mapper.RatingMapper;
import org.springframework.stereotype.Service;

/**
* @author champion
* @description 针对表【rating】的数据库操作Service实现
* @createDate 2025-03-01 14:46:53
*/
@Service
public class RatingMybatisServiceImpl extends ServiceImpl<RatingMapper, Rating>
    implements RatingMybatisService {

}




