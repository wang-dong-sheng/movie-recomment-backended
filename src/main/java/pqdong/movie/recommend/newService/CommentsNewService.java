package pqdong.movie.recommend.newService;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.data.dto.comments.CommentSearchDto;
import pqdong.movie.recommend.data.dto.comments.CommentsDto;
import pqdong.movie.recommend.data.dto.comments.CommentsQueryRequest;
import pqdong.movie.recommend.mongo.utils.Constant;
import pqdong.movie.recommend.temp.CommentsTemp;
import pqdong.movie.recommend.temp.MovieTemp;
import pqdong.movie.recommend.temp.UserTemp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CommentsNewService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> commentsCollection;
    private MongoCollection<Document> movieCllection;
    private MongoCollection<Document> userCollection;

    private MongoCollection<Document> getMovieCollection() {
        if (null == movieCllection)
            movieCllection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Movie");
        return movieCllection;
    }

    private MongoCollection<Document> getUserCollection() {
        if (null == userCollection)
            userCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_COLLECTION);
        return userCollection;
    }

    private MongoCollection<Document> getCommentsCollection() {
        if (null == commentsCollection)
            commentsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Comment");
        return commentsCollection;
    }

    private CommentsTemp documentToComment(Document document) {
        CommentsTemp comment = null;
        try {
            comment =objectMapper.convertValue(document, CommentsTemp.class);
        } catch (Exception e) {
            log.error("转换评论文档失败", e);
        }
        return comment;
    }

    public Page<CommentsDto> getCommentList(CommentSearchDto commentSearchDto) {
        Long movieId = commentSearchDto.getMovieId();
        Integer userId = commentSearchDto.getUserId();
        String movieName = commentSearchDto.getMovieName();
        Date[] dateRange = commentSearchDto.getDateRange();
        long current = commentSearchDto.getCurrent();
        long pageSize = commentSearchDto.getPageSize();
        String userName = commentSearchDto.getUserName();

        // 构建查询条件
        List<Document> conditions = new ArrayList<>();
        if (movieId != null) {
            conditions.add(new Document("movieId", movieId));
        }
        if (userId!=null) {
            conditions.add(new Document("userId", userId));
        }
        if (StringUtils.isNotBlank(movieName)) {
            conditions.add(new Document("movieName", new Document("$regex", movieName)));
        }
        if (StringUtils.isNotBlank(userName)) {
            conditions.add(new Document("userName", new Document("$regex", userName)));
        }
        if (dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null) {
            conditions.add(new Document("commentTime", new Document("$gte", dateRange[0]).append("$lte", dateRange[1])));
        }

        Document query = conditions.isEmpty() ? new Document() : new Document("$and", conditions);
        
        // 执行查询
        FindIterable<Document> documents = getCommentsCollection().find(query)
            .skip((int) ((current - 1) * pageSize))
            .limit((int) pageSize);

        // 获取总记录数
        long total =getCommentsCollection().count(query);

        // 转换结果
        List<CommentsDto> comments = new ArrayList<>();
        for (Document doc : documents) {
            CommentsTemp comment = documentToComment(doc);
            Integer mId = comment.getMovieId();
            Integer uId = comment.getUserId();
            Document userDoc = getUserCollection().find(new Document("userId", uId)).first();
            UserTemp user=null;
            try {
                user = objectMapper.readValue(JSON.serialize(userDoc), UserTemp.class);
            } catch (IOException e) {
                log.error("转换评论文档失败", e);
            }
            Document movieDoc = getMovieCollection().find(new Document("movieId", mId)).first();
            MovieTemp movie=null;
            try {
                movie = objectMapper.readValue(JSON.serialize(movieDoc), MovieTemp.class);
            } catch (IOException e) {
                log.error("转换评论文档失败", e);
            }
            CommentsDto commentsDto = BeanUtil.copyProperties(comment, CommentsDto.class);
            if (user!=null){
                commentsDto.setUserName(user.getUserNickname());
            }
            if (movie!=null){
                commentsDto.setMovieName(movie.getName());
            }
            if (comment != null) {
                comments.add(commentsDto);
            }
        }

        // 构建分页结果
        Page<CommentsDto> page = new Page<>(current, pageSize);
        page.setTotal(total);
        page.setRecords(comments);
        return page;
    }

// ... existing code ...

    public Boolean addComment(CommentsTemp comment) {
        try {
            // 检查是否已存在相同评论
            Document existing = getCommentsCollection().find(
                    new Document("movieId", comment.getMovieId())
                            .append("userId", comment.getUserId())
                            .append("content", comment.getContent())
            ).first();

            if (existing != null) {
                log.warn("评论已存在");
                return false;
            }

            // 插入新评论
            getCommentsCollection().insertOne(Document.parse(objectMapper.writeValueAsString(comment)));
            return true;
        } catch (Exception e) {
            log.error("添加评论失败", e);
            return false;
        }
    }

    public Boolean deleteComment(String commentId) {
        try {
            getCommentsCollection().deleteOne(new Document("_id", commentId));
            return true;
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return false;
        }
    }

    public Boolean deleteComments(List<String> commentIds) {
        try {
            getCommentsCollection().deleteMany(new Document("_id", new Document("$in", commentIds)));
            return true;
        } catch (Exception e) {
            log.error("批量删除评论失败", e);
            return false;
        }
    }

    public Boolean updateComment(CommentsTemp comment) {
        try {
            // 检查评论是否存在
            Document existing = getCommentsCollection().find(new Document("_id", comment.get_id())).first();
            if (existing == null) {
                log.warn("评论不存在");
                return false;
            }

            // 更新评论
            getCommentsCollection().updateOne(
                    new Document("_id", comment.get_id()),
                    new Document("$set", Document.parse(objectMapper.writeValueAsString(comment)))
            );
            return true;
        } catch (Exception e) {
            log.error("更新评论失败", e);
            return false;
        }
    }

    public Boolean updateCommentVotes(String commentId, int votes) {
        try {
            getCommentsCollection().updateOne(
                    new Document("_id", commentId),
                    new Document("$set", new Document("votes", votes))
            );
            return true;
        } catch (Exception e) {
            log.error("更新评论投票数失败", e);
            return false;
        }
    }

    public CommentsTemp getCommentById(String commentId) {
        Document document = getCommentsCollection().find(new Document("_id", commentId)).first();
        if (document == null || document.isEmpty()) {
            return null;
        }
        return documentToComment(document);
    }

// ... existing code ...
}