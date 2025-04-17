package pqdong.movie.recommend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import pqdong.movie.recommend.data.entity.Comments;

import java.util.List;

/**
* @author champion
* @description 针对表【comments】的数据库操作Mapper
* @createDate 2025-03-01 13:21:31
* @Entity generator.domain.Comments
*/
public interface CommentsMapper extends BaseMapper<Comments> {
    List<Comments> selectCommentsByRating();

}




