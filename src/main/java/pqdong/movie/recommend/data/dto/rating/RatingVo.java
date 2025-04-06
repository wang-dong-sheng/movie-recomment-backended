package pqdong.movie.recommend.data.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingVo {

    private Long movieId;

    private String userMd;
    private Long userId;

    private Double rating;
}
