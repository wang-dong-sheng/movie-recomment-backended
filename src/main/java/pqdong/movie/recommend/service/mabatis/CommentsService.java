package pqdong.movie.recommend.service.mabatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import pqdong.movie.recommend.data.dto.comments.CommentSearchDto;
import pqdong.movie.recommend.data.dto.comments.CommentsQueryRequest;
import pqdong.movie.recommend.data.entity.Comments;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author champion
* @description 针对表【comments】的数据库操作Service
* @createDate 2025-02-16 12:03:03
*/
public interface CommentsService extends IService<Comments> {

    public Page<Comments> getCommentList(CommentSearchDto commentSearchDto);

    /**
     * 按条件获取评论
     *
     * @param commentsQueryRequest
     * @return
     */
    Object filterComments(CommentsQueryRequest commentsQueryRequest);
}
