package pqdong.movie.recommend.mongo.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import pqdong.movie.recommend.data.entity.Movie;

@Data
public class MovieMongo {

    @JsonIgnore
    private String _id;

    private int mid;

    private String name;

    private String descri;

    private String timelong;

    private String issue;

    private String shoot;

    private Double score;

    private String language;

    private String genres;

    private String actors;

    private String directors;

  public Movie movieMongoToMovie(){
      Movie movie = new Movie();
      movie.setMovieId(Long.valueOf(this.mid));
      movie.setActors(actors);
      movie.setDirectors(directors);
      movie.setGenres(genres);
      String[] split = this.timelong.split(" ");
      movie.setMins(Integer.valueOf(split[0]));
      movie.setName(this.name);
      movie.setRegions(issue);

      movie.setStoryline(this.descri);
      movie.setYear(Integer.valueOf(shoot));
      movie.setIsUp(Short.parseShort("1"));
      return movie;
      
  }
}