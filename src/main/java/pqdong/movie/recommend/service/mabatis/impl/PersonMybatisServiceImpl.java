package pqdong.movie.recommend.service.mabatis.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import pqdong.movie.recommend.temp.PersonTemp;
import pqdong.movie.recommend.service.mabatis.PersonMybatisService;
import pqdong.movie.recommend.mapper.PersonMapper;
import org.springframework.stereotype.Service;

/**
* @author champion
* @description 针对表【person】的数据库操作Service实现
* @createDate 2025-04-19 16:56:47
*/
@Service
public class PersonMybatisServiceImpl extends ServiceImpl<PersonMapper, PersonTemp>
    implements PersonMybatisService {

}




