package pqdong.movie.recommend.data.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import pqdong.movie.recommend.data.entity.CommentEs;

import java.util.List;

//@Repository
//public interface CommentEsRepo extends ElasticsearchRepository<CommentEs, Long> {
//    List<CommentEs> findByMovieId(Long movieId);
//
//    List<CommentEs> findByUserMd(String userMd);
//}