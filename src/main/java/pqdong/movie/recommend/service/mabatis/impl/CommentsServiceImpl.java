package pqdong.movie.recommend.service.mabatis.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.data.dto.comments.CommentSearchDto;
import pqdong.movie.recommend.data.dto.comments.CommentsQueryRequest;
import pqdong.movie.recommend.data.entity.Comments;
import pqdong.movie.recommend.mapper.CommentsMapper;
import pqdong.movie.recommend.service.mabatis.CommentsService;

import java.util.Date;

/**
 * @author champion
 * @description 针对表【comments】的数据库操作Service实现
 * @createDate 2025-02-16 12:03:03
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
        implements CommentsService {

    //     获取评论列表
    public Page<Comments> getCommentList(CommentSearchDto commentSearchDto) {

//        String userId = commentSearchDto.getUserId();
        Long movieId = commentSearchDto.getMovieId();
        String userMd = commentSearchDto.getUserMd();
        String movieName = commentSearchDto.getMovieName();
        Date[] dateRange = commentSearchDto.getDateRange();
        long current = commentSearchDto.getCurrent();
        long pageSize = commentSearchDto.getPageSize();
        String userName = commentSearchDto.getUserName();
        Page<Comments> commentsPage = new Page<>(current, pageSize);
        QueryWrapper<Comments> wrapper = new QueryWrapper<>();
        wrapper.eq(movieId!=null,"movie_id",movieId);
//        wrapper.eq(userId!=null,"user_id",userId);
        wrapper.eq(StringUtils.isNotBlank(userMd),"user_md",userMd);
        wrapper.like(StringUtils.isNotBlank(movieName),"movie_name",movieName);
        wrapper.like(StringUtils.isNotBlank(userName),"user_name",userName);
        boolean isRange = dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null;
        if (isRange) {
            wrapper.between(isRange, "DATE(commentTime)", dateRange[0], dateRange[1]);
        }
        Page<Comments> resPage = this.page(commentsPage, wrapper);
        return resPage;
    }

    @Override
    public Object filterComments(CommentsQueryRequest commentsQueryRequest) {
        return null;
    }

}




