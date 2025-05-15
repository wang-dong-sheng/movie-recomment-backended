package pqdong.movie.recommend.config;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

/**
 * redis的连接配置
 */
@Configuration
public class RedisAndMongoConfigure {

    @Value("${mongo.host}")
    private String mongoHost;
    @Value("${mongo.port}")
    private int mongoPort;
    @Value("${es.cluster.name}")
    private String esClusterName;
    @Value("${es.host}")
    private String esHost;
    @Value("${es.port}")
    private int esPort;
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.database:0}") // 从配置文件中注入数据库索引
    private int redisDatabase;


    @Bean(name = "mongoClient")
    public MongoClient getMongoClient(){
        MongoClient mongoClient = new MongoClient( mongoHost , mongoPort );
        return mongoClient;
    }



    @Bean(name = "jedis")
    public Jedis getRedisClient() {
        Jedis jedis = new Jedis(redisHost);
        jedis.select(redisDatabase); // 选择指定数据库
        return jedis;
    }
}
