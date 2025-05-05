package pqdong.movie.recommend.utils;/**
 * @author Mr.Wang
 * @create 2025-05-05-15:52
 */

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pqdong.movie.recommend.data.constant.RedisAnalysisConstant;
import pqdong.movie.recommend.mongo.model.recom.Recommendation;
import pqdong.movie.recommend.mongo.service.RecommenderService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 *@ClassName AnalysisRedisUtils
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/5 15:52
 *@Version 1.0
 */
@Component
public class AnalysisRedisUtils {
    @Resource
    private RecommenderService recommenderService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 将请求设置到实时推荐全部统计中
     * @param key
     * @param movieId
     * @param userId
     */
    public void setAllSet(String key,String lenKey,String movieId,Integer userId ){
        //1.拿到当前推荐用户推荐列表
        List<Recommendation> streamRecs = recommenderService.findStreamRecs(userId, 4);
        if (streamRecs==null||streamRecs.isEmpty()) return;
        List<String> movieIds = streamRecs.stream().map(movie->String.valueOf(movie.getMid())
        ).collect(Collectors.toList());
        //2.将当前推荐列表存入redis的set集合

        stringRedisTemplate.opsForSet().add(key,movieIds.toArray(new String[0]));
        //记录总的长度
        Long userIdallLen = this.getSetLen(RedisAnalysisConstant.ALL_CURRENT_PREFIX, userId);
        stringRedisTemplate.opsForHash().put(lenKey,String.valueOf(userId),String.valueOf(userIdallLen));

//        stringRedisTemplate.opsForSet().add(key,String.valueOf(movieId));
    }
    /**
     * 将请求设置到实时推荐的有效点击计数中
     * @param movieId
     */
    public void setRealSet(String allKey,String realKey,String lenKey,String movieId,Integer userId){
        List<Recommendation> streamRecs = recommenderService.findStreamRecs(userId, 4);
        if (streamRecs==null||streamRecs.isEmpty()) return;
        //1.拿到当前推荐用户推荐列表
        Boolean isExit = stringRedisTemplate.opsForSet().isMember(allKey, String.valueOf(movieId));
        //2.将当前推荐列表存入redis的set集合
        if (isExit){
            stringRedisTemplate.opsForSet().add(realKey,String.valueOf(movieId));
            //记录总的长度
            Long userIdallLen = this.getSetLen(RedisAnalysisConstant.REAL_CURRENT_PREFIX, userId);
            stringRedisTemplate.opsForHash().put(lenKey,String.valueOf(userId),String.valueOf(userIdallLen));

        }
    }

    public Long getSetLen(String prefix,Integer userId){
        DateTime now = DateUtil.date();
        // 获取年份和月份
        int year = now.year();
        int month = now.month();
        //设计set前缀
        String key= prefix+year+":"+month+":"+userId;
        Long size = stringRedisTemplate.opsForSet().size(key);
        return size;
    }

//    获取hash集合中所有value之和
    public Long getHashLen(String key){
        List<Object> values = stringRedisTemplate.opsForHash().values(key);
        Long sum=0L;
        for (Object value : values) {
            Integer item=Integer.valueOf(String.valueOf(value));
            sum+=item;
        }
        return sum;
    }
}


