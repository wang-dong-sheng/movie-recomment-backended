package pqdong.movie.recommend.data.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingVo {

    private Long movieId;

    private Long userId;

    private Float rating;
}
