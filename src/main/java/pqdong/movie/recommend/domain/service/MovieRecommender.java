package pqdong.movie.recommend.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MovieRecommender {
    private final static int NEIGHBORHOOD_NUM = 3;
    @Resource(name = "mySQLDataModel")
    private DataModel dataModel;

    private List<Long> getRecommendedItemIDs(List<RecommendedItem> recommendations){
        List<Long> recommendItems = new ArrayList<>();
        for(int i = 0 ; i < recommendations.size() ; i++) {
            RecommendedItem recommendedItem=recommendations.get(i);
            recommendItems.add(recommendedItem.getItemID());
        }
        return recommendItems;
    }

    // 基于用户的推荐算法
    public List<Long> userBasedRecommender(long userID,int size) throws TasteException {
        UserSimilarity similarity  = new PearsonCorrelationSimilarity(dataModel );
        NearestNUserNeighborhood  neighbor = new NearestNUserNeighborhood(NEIGHBORHOOD_NUM, similarity, dataModel );
        Recommender recommender = new CachingRecommender(new GenericUserBasedRecommender(dataModel , neighbor, similarity));
        List<RecommendedItem> recommendations = recommender.recommend(userID, size);
        for (RecommendedItem item : recommendations) {
            log.info("推荐电影: " + item.getItemID() + "，预测评分: " + item.getValue());
        }
        return getRecommendedItemIDs(recommendations);
    }

    // 基于内容的推荐算法
    public List<Long> itemBasedRecommender(long userID,int size) throws TasteException {
        ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
        Recommender recommender = new GenericItemBasedRecommender(dataModel, itemSimilarity);
        List<RecommendedItem> recommendations = recommender.recommend(userID, size);
        return getRecommendedItemIDs(recommendations);
    }


//    int targetUserId = 49;    // 目标用户ID
//    int numRecommendations = 3; // 推荐数量

    /**
     * LogLikelihoodSimilarity（推荐默认）
     *
     * 优点：适合隐式反馈数据（如点击、浏览），对数据稀疏性不敏感
     *
     * 场景：当使用评分数据时，可以视为"用户对物品有兴趣"的隐式反馈
     * @param targetUserId
     * @param numRecommendations
     * @return
     * @throws TasteException
     */
    public List<Long> itemBaseLike(long targetUserId,int numRecommendations) throws TasteException {
        // 3. 构建物品相似度计算器（核心修改点）
        ItemSimilarity similarity = new LogLikelihoodSimilarity(dataModel);

        // 4. 创建基于物品的推荐器
        Recommender recommender = new GenericItemBasedRecommender(dataModel, similarity);

        // 5. 生成推荐（参数保持不变）
        List<RecommendedItem> recommendations = recommender.recommend(targetUserId, numRecommendations);

        System.out.println("用户 " + targetUserId + " 的推荐结果：");
        for (RecommendedItem item : recommendations) {
            System.out.println("推荐电影: " + item.getItemID() + "，预测评分: " + item.getValue());
        }
        return getRecommendedItemIDs(recommendations);
    }

}

