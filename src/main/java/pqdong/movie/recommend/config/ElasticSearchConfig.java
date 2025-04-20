package pqdong.movie.recommend.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pqdong.movie.recommend.service.jpa.ConfigService;

import javax.annotation.Resource;

@Configuration
public class ElasticSearchConfig {

//    @Resource
//    private ConfigService configService;
//
//    @Value("${spring.data.elasticsearch.rest.uris}")
//    private String esHost;
//
//    @Value("${spring.data.elasticsearch.rest.username}")
//    private String username;
//
//    @Value("${spring.data.elasticsearch.rest.password}")
//    private String password;

    @Value("${spring.data.elasticsearch.rest.uris}")
    private String esHost;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
            RestClient.builder(
                HttpHost.create(esHost)
            )
        );
    }
}
