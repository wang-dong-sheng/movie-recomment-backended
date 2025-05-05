package pqdong.movie.recommend.newService;/**
 * @author Mr.Wang
 * @create 2025-05-05-16:13
 */

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.data.constant.RedisAnalysisConstant;
import pqdong.movie.recommend.data.dto.analysis.AnalysisDto;
import pqdong.movie.recommend.data.dto.analysis.AnalysisVo;
import pqdong.movie.recommend.temp.UserTemp;
import pqdong.movie.recommend.utils.AnalysisRedisUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *@ClassName AnalysisService
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/5 16:13
 *@Version 1.0
 */
@Service
public class AnalysisService {
    @Resource
    private AnalysisRedisUtils analysisRedisUtils;
    public List<AnalysisDto> getAnalysis(List<AnalysisVo> analysisVoList) {
        List<AnalysisDto> analysisDtoList=new ArrayList<>();
        for (AnalysisVo analysisVo : analysisVoList) {
            AnalysisDto analysis = getAnalysis(analysisVo);
            analysisDtoList.add(analysis);
        }
        return analysisDtoList;


    }
    private AnalysisDto getAnalysis(AnalysisVo analysisVo){
        AnalysisDto analysisDto = new AnalysisDto();
        //1.获取当前时间拿到对应的年和月份
        // 获取当前时间
        Integer year = analysisVo.getYear();
        Integer month = analysisVo.getMonth();
        analysisDto.setYear(year);
        analysisDto.setMonth(month);
        String allAddLenKey=RedisAnalysisConstant.ALL_CURRENT_LEN_LIST_PREFIX+year+":"+month;
        String userAddLenKey=RedisAnalysisConstant.REAL_CURRENT_LEN_LIST_PREFIX+year+":"+month;
        Long allLen = analysisRedisUtils.getHashLen(allAddLenKey);
        Long userLen = analysisRedisUtils.getHashLen(userAddLenKey);
        if (allLen==null||userLen==null||allLen==0L||userLen==0L){
//            不存在
            return analysisDto;
        }
        analysisDto.setRealCurrentTimeTotal(allLen);
        analysisDto.setAllCurrentTimeTotal(userLen);
        analysisDto.setRate(userLen/allLen.doubleValue());

        return  analysisDto;
    }

}


