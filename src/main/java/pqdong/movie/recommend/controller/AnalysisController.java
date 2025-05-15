package pqdong.movie.recommend.controller;/**
 * @author Mr.Wang
 * @create 2025-05-05-16:10
 */

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pqdong.movie.recommend.annotation.LoginRequired;
import pqdong.movie.recommend.data.dto.analysis.AnalysisDto;
import pqdong.movie.recommend.data.dto.analysis.AnalysisVo;
import pqdong.movie.recommend.common.ResponseMessage;
import pqdong.movie.recommend.service.AnalysisService;

import javax.annotation.Resource;
import java.util.List;

/**
 *@ClassName AnalysisController
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/5 16:10
 *@Version 1.0
 */
@RestController
@RequestMapping("/movie")
public class AnalysisController {
    @Resource
    private AnalysisService analysisService;
    @PostMapping("/analysis")
    @LoginRequired
    public ResponseMessage<List<AnalysisDto>> getAnalysis(@RequestBody List<AnalysisVo> analysisVoList) {
        return ResponseMessage.successMessage(analysisService.getAnalysis(analysisVoList));
    }
}


