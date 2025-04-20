package pqdong.movie.recommend.newService;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pqdong.movie.recommend.common.ErrorCode;
import pqdong.movie.recommend.common.PageRequest;
import pqdong.movie.recommend.data.constant.UserConstant;
import pqdong.movie.recommend.data.dto.user.UserInfo;
import pqdong.movie.recommend.data.dto.user.UserQueryRequest;
import pqdong.movie.recommend.exception.ThrowUtils;
import pqdong.movie.recommend.mongo.utils.Constant;
import pqdong.movie.recommend.redis.RedisApi;
import pqdong.movie.recommend.redis.RedisKeys;
import pqdong.movie.recommend.service.jpa.QiNiuService;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.Md5EncryptionHelper;
import pqdong.movie.recommend.utils.RecommendUtils;
import pqdong.movie.recommend.utils.ThreadLocalUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserNewService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisApi redisApi;

    @Autowired
    private QiNiuService qiNiuService;

    private MongoCollection<Document> userCollection;

    private MongoCollection<Document> getUserCollection() {
        if (null == userCollection)
            userCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("User");
        return userCollection;
    }

    private UserTemp documentToUser(Document document) {
        UserTemp user = null;
        try {
            user = objectMapper.readValue(JSON.serialize(document), UserTemp.class);
        } catch (IOException e) {
            log.error("转换用户文档失败", e);
        }
        return user;
    }

    public Page<UserTemp> getAllUser(PageRequest pageRequest) {
        long pageSize = pageRequest.getPageSize();
        long current = pageRequest.getCurrent();

        FindIterable<Document> documents = getUserCollection()
                .find()
                .skip((int) ((current - 1) * pageSize))
                .limit((int) pageSize);

        long total = getUserCollection().count();

        List<UserTemp> users = new ArrayList<>();
        for (Document doc : documents) {
            users.add(documentToUser(doc));
        }

        Page<UserTemp> page = new Page<>(current, pageSize);
        page.setTotal(total);
        page.setRecords(users);
        return page;
    }

    public Boolean deleteUsers(List<Long> ids) {
        Long currentId = ThreadLocalUtils.getCurrentId();
        ThrowUtils.throwIf(ids.contains(currentId), ErrorCode.NO_AUTH_ERROR, "不能删除自己");

        // 检查是否有管理员
        for (Long id : ids) {
            Document userDoc = getUserCollection().find(new Document("id", id)).first();
            if (userDoc != null) {
                String userRole = userDoc.getString("userRole");
                ThrowUtils.throwIf(UserConstant.ADMIN_ROLE.equals(userRole), ErrorCode.NO_AUTH_ERROR, "不能删除管理员");
            }
        }

        try {
            getUserCollection().deleteMany(new Document("id", new Document("$in", ids)));
            return true;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return false;
        }
    }

    public Page<UserTemp> filterUsers(UserQueryRequest userQueryRequest) {
        String userNickname = userQueryRequest.getUserNickname();
        Long userId = userQueryRequest.getUserId();
        Date[] dateRange = userQueryRequest.getDateRange();

        List<Document> conditions = new ArrayList<>();
        if (StringUtils.isNotBlank(userNickname)) {
            conditions.add(new Document("userNickname", new Document("$regex", userNickname)));
        }
        if (userId != null) {
            conditions.add(new Document("id", userId));
        }
        if (dateRange != null && dateRange.length == 2 && dateRange[0] != null && dateRange[1] != null) {
            conditions.add(new Document("createTime",
                    new Document("$gte", dateRange[0]).append("$lte", dateRange[1])));
        }

        Document query = conditions.isEmpty() ? new Document() : new Document("$and", conditions);

        FindIterable<Document> documents = getUserCollection()
                .find(query)
                .skip((int) ((userQueryRequest.getCurrent() - 1) * userQueryRequest.getPageSize()))
                .limit((int) userQueryRequest.getPageSize());

        long total = getUserCollection().count(query);

        List<UserTemp> users = new ArrayList<>();
        for (Document doc : documents) {
            users.add(documentToUser(doc));
        }

        Page<UserTemp> page = new Page<>(userQueryRequest.getCurrent(), userQueryRequest.getPageSize());
        page.setTotal(total);
        page.setRecords(users);
        return page;
    }

    public UserTemp updateUser(UserTemp user) {
        try {
            // 检查用户名是否已存在
            Document existing = getUserCollection().find(
                new Document("userNickname", user.getUserNickname())
            ).first();

            if (existing != null ) {
                UserTemp existingUser = documentToUser(existing);
                if (!existingUser.getUserId().equals(user.getUserId())){
                    return null;
                }

            }

            // 构建更新文档（仅包含非null字段）
            Document updateFields = new Document();

            // 基础字段更新
            if (user.getPassword() != null) {
                updateFields.append("password", Md5EncryptionHelper.getMD5WithSalt(user.getPassword()));
            }
            if (user.getUserAvatar() != null) {
                updateFields.append("userAvatar", user.getUserAvatar());
            }
            if (user.getUserNickname() != null) {
                updateFields.append("userNickname", user.getUserNickname());
            }
            if (user.getUserTags() != null) {
                updateFields.append("userTags", user.getUserTags());
            }
            if (user.getPhone() != null) {
                updateFields.append("phone", user.getPhone());
            }
            if (user.getMotto() != null) {
                updateFields.append("motto", user.getMotto());
            }
            if (user.getSex() != null) {
                updateFields.append("sex", user.getSex());
            }
            if (user.getUserRole() != null) {
                updateFields.append("userRole", user.getUserRole());
            }

            // 时间字段更新（createTime通常不更新）
            if (user.getCreateTime() != null) {
                updateFields.append("createTime", user.getCreateTime());
            }


            // 集合字段更新
            if (user.getPrefGenres() != null && !user.getPrefGenres().isEmpty()) {
                updateFields.append("prefGenres", user.getPrefGenres());
                // 特殊字段更新（假设first是Boolean类型）
                updateFields.append("first", false);
            }

            // 执行更新操作
            if (!updateFields.isEmpty()) {
                UpdateResult result = getUserCollection().updateOne(
                        new Document("userId", user.getUserId()),
                        new Document("$set", updateFields)
                );

                // 自动更新updateTime（推荐）
                if (result.getModifiedCount() > 0) {
                    getUserCollection().updateOne(
                            new Document("userId", user.getUserId()),
                            new Document("$set", new Document("updateTime", (int)(System.currentTimeMillis()/1000)))
                    );
                }
                Document updateUserDoc = getUserCollection().find(
                        new Document("userId", user.getUserId())
                ).first();
                UserTemp updateUser = documentToUser(updateUserDoc);
                return updateUser;
            }

            return null; // 没有需要更新的字段
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return null;
        }
    }

    public boolean registerUser(UserInfo userInfo) {

        String pass = Md5EncryptionHelper.getMD5WithSalt(userInfo.getPassword());
        String randomAvatar = RecommendUtils.getRandomAvatar(userInfo.getUsername());
        UserTemp user = new UserTemp();
        user.setUserAvatar(randomAvatar);
        user.setUserNickname(userInfo.getUsername());
        user.setPassword(pass);
        user.setFirst(true);
        user.setSex("男");
        user.setUserRole("user");
        user.setCreateTime(String.valueOf(System.currentTimeMillis()));

        try {
            // 1. 获取用户集合
            MongoCollection<Document> collection = getUserCollection();

            // 2. 查询当前最大userId（降序取第一条）
            FindIterable<Document> findIterable = collection.find()
                    .sort(Sorts.descending("userId"))
                    .limit(1);

            Document maxDoc = findIterable.first();
            int newUserId;

            // 3. 处理空集合情况
            if (maxDoc == null) {
                newUserId = 1; // 初始值建议从业务安全值开始（如10001）
            } else {
                newUserId = maxDoc.getInteger("userId", 0) + 1;
            }

            // 4. 设置新userId
            user.setUserId(newUserId);

            // 5. 插入新用户
            collection.insertOne(Document.parse(objectMapper.writeValueAsString(user)));
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkUserExist(String username) {
        return null != findByUsername(username);
    }

    public UserTemp findByUsername(String username) {
        Document user = getUserCollection().find(new Document("userNickname", username)).first();
        if (null == user || user.isEmpty())
            return null;
        return documentToUser(user);
    }

    public UserTemp getUserInfo(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        String userMd = redisApi.getString(RecommendUtils.getKey(RedisKeys.USER_TOKEN, token));
        if (StringUtils.isEmpty(userMd)) {
            return null;
        }

        Document document = getUserCollection().find(new Document("userMd", userMd)).first();
        return document != null ? documentToUser(document) : null;
    }

    public Map<String, Object> login(UserInfo userInfo) {
        Document userDoc = getUserCollection().find(
                new Document("userNickname", userInfo.getUsername())
        ).first();

        if (userDoc == null) {
            return null;
        }

        UserTemp user = documentToUser(userDoc);
        if (user.getPassword().equals(Md5EncryptionHelper.getMD5WithSalt(userInfo.getPassword()))) {
            Map<String, Object> info = new HashMap<>();
            String token = RecommendUtils.genToken();
            redisApi.setValue(RecommendUtils.getKey(RedisKeys.USER_TOKEN, token),
                    String.valueOf(user.getUserId()), 7, TimeUnit.DAYS);
            info.put("token", token);
            info.put("user", user);
            return info;
        }
        return null;
    }


    public String uploadAvatar(String userMd, MultipartFile avatar) {
        String name = RecommendUtils.getKey(UserConstant.USER_AVATAR, userMd);
        String url = qiNiuService.uploadPicture(avatar, name);

        Document userDoc = getUserCollection().find(new Document("userMd", userMd)).first();
        if (userDoc == null) {
            return "用户不存在";
        }

        getUserCollection().updateOne(
                new Document("userMd", userMd),
                new Document("$set", new Document("userAvatar", url))
        );

        return url;
    }

    public boolean logout() {
        return true;
    }
} 