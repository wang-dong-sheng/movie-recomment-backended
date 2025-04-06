package pqdong.movie.recommend.config;

import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;

public class MySQLMahoutExample {
    public static void main(String[] args) throws Exception {
        // 1. 配置数据库连接池
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/movieTest");
        config.setUsername("root");
        config.setPassword("abc123");
        HikariDataSource dataSource = new HikariDataSource(config);

        // 2. 初始化 DataModel
        DataModel model = new MySQLJDBCDataModel(
            dataSource,
            "rating_test",
            "user_id",
            "movie_id",
            "rating",
            "rating_time"
        );

        // 3. 构建推荐器
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, similarity, model);
        Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        // 4. 生成推荐
        List<RecommendedItem> recommendations = recommender.recommend(13, 3);
        for (RecommendedItem item : recommendations) {
            System.out.println("推荐电影: " + item.getItemID() + "，预测评分: " + item.getValue());
        }

        dataSource.close();
    }
}