package pqdong.movie.recommend.config;

import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MahoutConfig {


    @Autowired
    private DataSource dataSource;

    @Bean(autowire = Autowire.BY_NAME,value = "mySQLDataModel")
    public DataModel getMySQLJDBCDataModel(){
        return new MySQLJDBCDataModel(dataSource,"rating_test","user_id",
                "movie_id","rating", "rating_time");
    }


//    @Bean(autowire = Autowire.BY_NAME,value = "fileDataModel")
//    public DataModel getDataModel() throws IOException {
//        URL url=MahoutConfig.class.getClassLoader().getResource("/rating.csv");
//        return new FileDataModel(new File(Objects.requireNonNull(url).getFile()));
//    }
}