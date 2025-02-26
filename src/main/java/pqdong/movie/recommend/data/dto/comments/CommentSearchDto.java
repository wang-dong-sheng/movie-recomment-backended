package pqdong.movie.recommend.data.dto.comments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pqdong.movie.recommend.common.PageRequest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentSearchDto extends PageRequest implements Serializable {
    private Integer page;

    private String userName;
    private Long userId;

    private Integer size;

    private Long movieId;
    private String movieName;

    /**
     *
     */
    private String content;

    /**
     *
     */
    private Integer votes;

    /**
     *
     */
    private Date commentTime;

    /**
     * 日期过滤
     */
    private Date[] dateRange;
    private static final long serialVersionUID = 1L;
}
