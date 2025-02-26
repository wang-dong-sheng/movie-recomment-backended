package pqdong.movie.recommend;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.Modifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.query.QueryEnhancerFactory;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Field;


@EnableAsync
@SpringBootApplication()
@Slf4j
public class MovieRecommendApplication {

    public static void main(String[] args) {
        try {
            QueryEnhancerFactory.forQuery(null);
        } catch (Exception e) {
            // just init class QueryEnhancerFactory, ignore anything
        }

        try {
            Field JSQLPARSER_IN_CLASSPATH = QueryEnhancerFactory.class.getDeclaredField("JSQLPARSER_IN_CLASSPATH");

            Field modifierField = Field.class.getDeclaredField("modifiers");
            modifierField.setAccessible(true);
            modifierField.setInt(JSQLPARSER_IN_CLASSPATH, JSQLPARSER_IN_CLASSPATH.getModifiers() & ~Modifier.FINAL);

            JSQLPARSER_IN_CLASSPATH.setAccessible(true);
            JSQLPARSER_IN_CLASSPATH.setBoolean(null, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        }
        SpringApplication.run(MovieRecommendApplication.class, args);
    }
}
