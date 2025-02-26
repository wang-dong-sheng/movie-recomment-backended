package pqdong.movie.recommend.config;

//import org.apache.http.HttpHost;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.TransportAddress;
//import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.springframework.beans.factory.annotation.Value;
import pqdong.movie.recommend.service.jpa.ConfigService;

import javax.annotation.Resource;

public class ElasticSearchConfig {

    @Resource
    private ConfigService configService;

    @Value("${spring.elasticsearch.ip}")
    private String ip;
//    @Bean
//    public RestHighLevelClient client(){
//        return  new RestHighLevelClient(RestClient.builder(
//                HttpHost.create("http://"+ip+":9200")
//        ));
//    }

//    @Bean
//    public TransportClient transportClient(@Value("${spring.elasticsearch.ip}") String ip){
//        String password = configService.getConfigValue("ESPASSWORD");
//        try (TransportClient client = new PreBuiltXPackTransportClient(Settings.builder()
//                .put("cluster.name", "docker-cluster")
//                .put("xpack.security.user", password)
//                .put("timeout", 10000)
//                .put("client.transport.ping_timeout", 10000)
//                .build())
//                .addTransportAddress(new TransportAddress(new InetSocketAddress(ip, 9300)))) {
//            return client;
//        }
//    }
}
