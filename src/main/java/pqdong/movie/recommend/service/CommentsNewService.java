package pqdong.movie.recommend.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.util.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.data.dto.comments.CommentSearchDto;
import pqdong.movie.recommend.data.dto.comments.CommentsDto;
import pqdong.movie.recommend.constant.Constant;
import pqdong.movie.recommend.data.entity.CommentsTemp;
import pqdong.movie.recommend.data.entity.MovieTemp;
import pqdong.movie.recommend.data.entity.UserTemp;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
            Object _id = document.get("_id");
             comment = BeanUtil.copyProperties(document, CommentsTemp.class, "_id");

            comment.set_id(String.valueOf(_id));
        } catch (Exception e) {
            log.error("转换评论文档失败", e);
        }
        return comment;
    }

    public Page<CommentsDto> getCommentList(CommentSearchDto commentSearchDto) {
        String _id = commentSearchDto.get_id();
        Long movieId = commentSearchDto.getMovieId();
        Integer userId = commentSearchDto.getUserId();
        String movieName = commentSearchDto.getMovieName();
        Date[] dateRange = commentSearchDto.getDateRange();
        long current = commentSearchDto.getCurrent();
        long pageSize = commentSearchDto.getPageSize();
        String userName = commentSearchDto.getUserName();

        // 构建查询条件
        List<Bson> conditions = new ArrayList<>();

        if (!StringUtils.isBlank(_id)){
            ObjectId objectId = new ObjectId(_id);
            conditions.add(Filters.eq("_id",objectId));
        }
        if (movieId != null) {
            conditions.add(Filters.eq("movieId", movieId));
        }

        if (userId != null) {
            conditions.add(Filters.eq("userId", userId));
        }

        if (StringUtils.isNotBlank(movieName)) {
            Document movieDoc = getMovieCollection().find(Filters.eq("name", movieName)).first();
            if (movieDoc != null) {
                try {
                    MovieTemp movie = objectMapper.readValue(JSON.serialize(movieDoc), MovieTemp.class);
                    if (movie != null && movie.getMovieId() != null) {
                        conditions.add(Filters.eq("movieId", movie.getMovieId()));
                    }
                } catch (IOException e) {
                    log.error("转换电影文档失败", e);
                }
            }
        }

        if (StringUtils.isNotBlank(userName)) {
            Document userDoc = getUserCollection().find(Filters.eq("userNickname", userName)).first();
            if (userDoc != null) {
                try {
                    UserTemp user = objectMapper.readValue(JSON.serialize(userDoc), UserTemp.class);
                    if (user != null && user.getUserId() != null) {
                        conditions.add(Filters.eq("userId", user.getUserId()));
                    }
                } catch (IOException e) {
                    log.error("转换用户文档失败", e);
                }
            }
        }

        if (dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null) {
            conditions.add(Filters.and(
                    Filters.gte("commentTime", dateRange[0]),
                    Filters.lte("commentTime", dateRange[1])
            ));
        }

        Bson query = conditions.isEmpty() ? new Document() : Filters.and(conditions);

        // 执行查询
        FindIterable<Document> documents = getCommentsCollection().find(query)
                .skip((int) ((current - 1) * pageSize))
                .limit((int) pageSize);

        // 获取总记录数
        long total = getCommentsCollection().count(query);

        // 转换结果
        List<CommentsDto> comments = new ArrayList<>();
        for (Document doc : documents) {
            try {
                CommentsTemp comment = documentToComment(doc);
                if (comment == null) continue;

                Integer mId = comment.getMovieId();
                Integer uId = comment.getUserId();

                // 获取用户信息
                UserTemp user = null;
                if (uId != null) {
                    Document userDoc = getUserCollection().find(Filters.eq("userId", uId)).first();
                    if (userDoc != null) {
                        user = objectMapper.readValue(JSON.serialize(userDoc), UserTemp.class);
                    }
                }

                // 获取电影信息
                MovieTemp movie = null;
                if (mId != null) {
                    Document movieDoc = getMovieCollection().find(Filters.eq("movieId", mId)).first();
                    if (movieDoc != null) {
                        movie = objectMapper.readValue(JSON.serialize(movieDoc), MovieTemp.class);
                    }
                }

                CommentsDto commentsDto = BeanUtil.copyProperties(comment, CommentsDto.class);
                if (user != null) {
                    commentsDto.setUserName(user.getUserNickname());
                }
                if (movie != null) {
                    commentsDto.setMovieName(movie.getName());
                }
                comments.add(commentsDto);
            } catch (Exception e) {
                log.error("处理评论文档失败", e);
            }
        }

        // 构建分页结果
        Page<CommentsDto> page = new Page<>(current, pageSize);
        page.setTotal(total);
        page.setRecords(comments);
        return page;
    }


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
//            mongo中的_id类型为ObjectId，所以需要转换
            List<ObjectId> objectIds = commentIds.stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());
            DeleteResult id = getCommentsCollection().deleteMany(new Document("_id", new Document("$in", objectIds)));
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