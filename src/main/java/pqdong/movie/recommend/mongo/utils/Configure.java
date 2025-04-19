package pqdong.movie.recommend.mongo.utils;

import com.mongodb.MongoClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class Configure {

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

//    public Configure(){
//        try{
//            Properties properties = new Properties();
//            Resource resource = new ClassPathResource("recommend.properties");
//            properties.load(new FileInputStream(resource.getFile()));
//            this.mongoHost = properties.getProperty("mongo.host");
//            this.mongoPort = Integer.parseInt(properties.getProperty("mongo.port"));
//            this.esClusterName = properties.getProperty("es.cluster.name");
//            this.esHost = properties.getProperty("es.host");
//            this.esPort = Integer.parseInt(properties.getProperty("es.port"));
//            this.redisHost = properties.getProperty("redis.host");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            System.exit(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(0);
//        }
//    }

    @Bean(name = "mongoClient")
    public MongoClient getMongoClient(){
        MongoClient mongoClient = new MongoClient( mongoHost , mongoPort );
        return mongoClient;
    }

    @Bean(name = "transportClient")
    public TransportClient getTransportClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name",esClusterName).build();
        TransportClient esClient = new PreBuiltTransportClient(settings);
        esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
        return esClient;
    }

    @Bean(name = "jedis")
    public Jedis getRedisClient() {
        Jedis jedis = new Jedis(redisHost);
        jedis.select(redisDatabase); // 选择指定数据库
        return jedis;
    }
}
