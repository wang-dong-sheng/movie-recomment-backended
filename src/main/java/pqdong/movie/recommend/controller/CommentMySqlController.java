package pqdong.movie.recommend.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pqdong.movie.recommend.annotation.AuthCheck;
import pqdong.movie.recommend.annotation.LoginRequired;
import pqdong.movie.recommend.data.constant.UserConstant;
import pqdong.movie.recommend.data.dto.comments.CommentSearchDto;
import pqdong.movie.recommend.data.dto.comments.CommentsDto;
import pqdong.movie.recommend.data.entity.Comments;
import pqdong.movie.recommend.domain.util.ResponseMessage;
import pqdong.movie.recommend.newService.CommentsNewService;
import pqdong.movie.recommend.service.mabatis.CommentsService;
import pqdong.movie.recommend.temp.CommentsTemp;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentMySqlController {

//    @Resource
//    private CommentsService commentService;
    @Resource
    private CommentsNewService commentsNewService;

    /**
     * @method getCommentList 获取电影标签
     */
    @PostMapping("/list")
    public ResponseMessage<Page<CommentsDto>> getCommentList(@RequestBody CommentSearchDto commentSearchDto) {
        return ResponseMessage.successMessage(commentsNewService.getCommentList(commentSearchDto));
    }

    /**
     * @method submitComment 提交评论
     */
    @PostMapping("/submit")
    @LoginRequired
    public ResponseMessage submitComment(@RequestBody CommentsTemp comments) {
        commentsNewService.addComment(comments);
        return ResponseMessage.successMessage(comments);
    }
    /**
     * @method submitComment 提交评论
     */
    @DeleteMapping("/deleteComments")
    @LoginRequired
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ResponseMessage<Boolean> deleteComments(@RequestBody List<String> ids) {
        boolean b = commentsNewService.deleteComments(ids);
        log.info("");
        return ResponseMessage.successMessage(b);
    }

    /**
     * 按条件查询用户相关信息
     *
     */
//    @PostMapping("/filterUsers")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public ResponseMessage<Page<User>> filterUsers(@RequestBody CommentsQueryRequest commentsQueryRequest){
//
//        log.info(JSONUtil.toJsonStr(commentsQueryRequest));
//
//        return ResponseMessage.successMessage(commentService.filterComments(commentsQueryRequest));
//    }
}
