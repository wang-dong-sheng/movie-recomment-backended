package pqdong.movie.recommend.data.dto.analysis;/**
 * @author Mr.Wang
 * @create 2025-05-05-15:49
 */

import lombok.Data;

/**
 *@ClassName AnalysisDto
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/5 15:49
 *@Version 1.0
 */
@Data
public class AnalysisDto {
    private Integer year;
    private Integer month;
    private Long realCurrentTimeTotal;
    private Long allCurrentTimeTotal;
    private Double rate;

}


