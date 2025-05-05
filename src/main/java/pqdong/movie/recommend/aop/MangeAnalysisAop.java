package pqdong.movie.recommend.aop;/**
 * @author Mr.Wang
 * @create 2025-05-05-14:56
 */

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pqdong.movie.recommend.annotation.Analysis;
import pqdong.movie.recommend.annotation.AuthCheck;
import pqdong.movie.recommend.common.ErrorCode;
import pqdong.movie.recommend.data.constant.RedisAnalysisConstant;
import pqdong.movie.recommend.data.dto.rating.RatingVo;
import pqdong.movie.recommend.enums.UserRoleEnum;
import pqdong.movie.recommend.exception.BusinessException;
import pqdong.movie.recommend.exception.MyException;
import pqdong.movie.recommend.exception.ResultEnum;
import pqdong.movie.recommend.mongo.model.recom.Recommendation;
import pqdong.movie.recommend.mongo.service.RecommenderService;
import pqdong.movie.recommend.mongo.service.UserMongoService;
import pqdong.movie.recommend.newService.MovieNewService;
import pqdong.movie.recommend.newService.UserNewService;
import pqdong.movie.recommend.redis.RedisApi;
import pqdong.movie.recommend.redis.RedisKeys;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.AnalysisRedisUtils;
import pqdong.movie.recommend.utils.RecommendUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName MangeAnalysisAop
 * @Description TODO
 * @Author Mr.Wang
 * @Date 2025/5/5 14:56
 * @Version 1.0
 */
@Aspect
@Component
public class MangeAnalysisAop {

    @Resource
    private UserNewService userNewService;

    @Resource
    private AnalysisRedisUtils analysisRedisUtils;


    /**
     * 执行拦截
     * 直接拦截查看电影详情信息，每次都去查一遍对应的推荐表，然后放入redis记录就好了
     *
     * @param joinPoint
     * @param
     * @return
     */
    @Around("@annotation(analysis)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, Analysis analysis) throws Throwable {
//        String mustRole = authCheck.mustRole();/**/
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        Object[] args = joinPoint.getArgs();
        // 方法只有一个参数 movieId
        RatingVo ratingVo = (RatingVo)args[0];
        Long userId = ratingVo.getUserId();

        if (userId==null){
            //说明用户没登陆或者用户最近没评分过那么就没有实时数据直接放行不做统计
            return joinPoint.proceed();
        }
        Long movieId = ratingVo.getMovieId();
        //拿到该用户的最近set集合数据，放入本次访问的movieId且本次movieId在推荐列表中
        //1.获取当前时间拿到对应的年和月份
        // 获取当前时间
        DateTime now = DateUtil.date();
        // 获取年份和月份
        int year = now.year();
        int month = now.month()+1;
        //设计set前缀
        String userAddKey= RedisAnalysisConstant.REAL_CURRENT_PREFIX+year+":"+month+":"+userId;
        String allAddKey= RedisAnalysisConstant.ALL_CURRENT_PREFIX+year+":"+month+":"+userId;
        String allAddLenKey=RedisAnalysisConstant.ALL_CURRENT_LEN_LIST_PREFIX+year+":"+month;
        String userAddLenKey=RedisAnalysisConstant.REAL_CURRENT_LEN_LIST_PREFIX+year+":"+month;

        //2.访问实时推荐表，更新all:curentTime:year:month:userId的数据即movieId
        analysisRedisUtils.setAllSet(allAddKey,allAddLenKey,String.valueOf(movieId),userId.intValue());
        //3.如果本次访问movieId在2的列表中那么将本次movieId存入real:curentTime:year:month:userId
        analysisRedisUtils.setRealSet(allAddKey,userAddKey,userAddLenKey,String.valueOf(movieId),userId.intValue());
        return joinPoint.proceed();
    }


    public static void main(String[] args) {
        // 获取当前时间
        DateTime now = DateUtil.date();

        // 获取年份和月份
        int year = now.year();
        int month = now.month();

        // 输出结果
        System.out.println("Year: " + year);
        System.out.println("Month: " + month);
    }



}


