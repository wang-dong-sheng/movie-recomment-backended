package pqdong.movie.recommend.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mr.Wang
 * @create 2025-04-14-11:46
 */
@SpringBootTest
class RatingMessageProducerTest {

    @Resource
    RatingMessageProducer producer;
    @Test
    void sendRatingMessage() {
        producer.sendRatingMessage("recommender", "2","300",3.0,835355532);
    }
}