package pqdong.movie.recommend.data.constant;/**
 * @author Mr.Wang
 * @create 2025-05-05-14:52
 */

/**
 *@InterfaceName RedisAnalizeConstant
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/5 14:52
 *@Version 1.0
 */
public interface RedisAnalysisConstant {
    //后面跟year:month:userId
    String REAL_CURRENT_PREFIX="analysis:total:real:currentTime:";
//    用于统计当月所有访问，后面接月份
    String REAL_CURRENT_LEN_LIST_PREFIX="analysis:len:real:currentTime:";
    //后面跟year:month:userId
    String ALL_CURRENT_PREFIX="analysis:total:all:currentTime:";
    //    用于统计当月所有访问，后面接月份
    String ALL_CURRENT_LEN_LIST_PREFIX="analysis:len:all:currentTime:";
}
