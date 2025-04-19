package pqdong.movie.recommend.mongo.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieHybridRecommendationRequest {
    private Integer userId;
    private int movieId;

    private int sum;

}
