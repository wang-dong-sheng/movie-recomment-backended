package pqdong.movie.recommend.mongo.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class User {

    @JsonIgnore
    private String _id;

    private int uid;

    private String username;

    private String password;

    private boolean first;

    private long timestamp;

    /**
     * 用户角色
     */
    private String userRole;

    private List<String> prefGenres = new ArrayList<>();

    public boolean passwordMatch(String password) {
        return this.password.compareTo(password) == 0;
    }
}