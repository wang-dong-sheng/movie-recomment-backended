package pqdong.movie.recommend.mongo.service;



import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import io.micrometer.core.instrument.util.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.data.dto.user.UserInfo;
import pqdong.movie.recommend.data.entity.UserEntity;
import pqdong.movie.recommend.domain.util.ResponseMessage;
import pqdong.movie.recommend.mongo.model.domain.User;
import pqdong.movie.recommend.mongo.model.request.LoginUserRequest;
import pqdong.movie.recommend.mongo.model.request.RegisterUserRequest;
import pqdong.movie.recommend.mongo.utils.Constant;
import pqdong.movie.recommend.redis.RedisApi;
import pqdong.movie.recommend.redis.RedisKeys;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.Md5EncryptionHelper;
import pqdong.movie.recommend.utils.RecommendUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserMongoService {
    @Resource
    private RedisApi redisApi;
    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> userCollection;

    private MongoCollection<Document> getUserCollection(){
        if(null == userCollection)
            userCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_COLLECTION);
        return userCollection;
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

    public Map<String, Object> loginUser(UserInfo user){
        if(null == user) {
            return null;
        }else if(user.getPassword().equals(Md5EncryptionHelper.getMD5WithSalt(user.getPassword()))){
            return null;
        }
        Map<String, Object> info = new HashMap<>();
        String token = RecommendUtils.genToken();
        UserTemp loginUser = findByUsername(user.getUsername());
        redisApi.setValue(RecommendUtils.getKey(RedisKeys.USER_TOKEN, token), String.valueOf(loginUser.getUserId()), 7, TimeUnit.DAYS);
        info.put("token", token);
        info.put("user", loginUser);
        return info;
    }

    private UserTemp documentToUser(Document document){
        try{
            return objectMapper.readValue(JSON.serialize(document),UserTemp.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkUserExist(String username){
        return null != findByUsername(username);
    }

    public UserTemp findByUsername(String username){
        Document user = getUserCollection().find(new Document("userNickname",username)).first();
        if(null == user || user.isEmpty())
            return null;
        return documentToUser(user);
    }

    public boolean updateUser(UserTemp user){
        getUserCollection().updateOne(Filters.eq("userId", user.getUserId()), new Document().append("$set",new Document("first", user.isFirst())));
        getUserCollection().updateOne(Filters.eq("userId", user.getUserId()), new Document().append("$set",new Document("prefGenres", user.getPrefGenres())));
        return true;
    }

    public UserTemp findByUID(int userId){
        Document user = getUserCollection().find(new Document("userId",userId)).first();
        if(null == user || user.isEmpty())
            return null;
        return documentToUser(user);
    }

    public void removeUser(String username){
        getUserCollection().deleteOne(new Document("username",username));
    }

    public UserTemp getUserInfo(String token) {

        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String userId = redisApi.getString(RecommendUtils.getKey(RedisKeys.USER_TOKEN, token));
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        return findByUID(Integer.valueOf(userId));
    }

    public boolean logout() {
        return true;
    }
}
