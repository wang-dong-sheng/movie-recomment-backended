package pqdong.movie.recommend.config;

import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;

public class MySQLItemBasedCFExample {
    public static void main(String[] args) throws Exception {
        // 1. 配置数据库连接池（保持不变）
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/movieTest");
        config.setUsername("root");
        config.setPassword("abc123");
        HikariDataSource dataSource = new HikariDataSource(config);

        // 2. 初始化 DataModel（保持不变）
        DataModel model = new MySQLJDBCDataModel(
            dataSource,
            "rating_test",
            "user_id",
            "movie_id",
            "rating",
            "rating_time"
        );

        // 3. 构建物品相似度计算器（核心修改点）
        ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
        
        // 4. 创建基于物品的推荐器
        Recommender recommender = new GenericItemBasedRecommender(model, similarity);

        // 5. 生成推荐（参数保持不变）
        int targetUserId = 13;    // 目标用户ID
        int numRecommendations = 3; // 推荐数量
        
        List<RecommendedItem> recommendations = recommender.recommend(targetUserId, numRecommendations);
        
        System.out.println("用户 " + targetUserId + " 的推荐结果：");
        for (RecommendedItem item : recommendations) {
            System.out.println("推荐电影: " + item.getItemID() + "，预测评分: " + item.getValue());
        }

        dataSource.close();
    }
}