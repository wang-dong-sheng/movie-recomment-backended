package pqdong.movie.recommend.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
@Component
@Slf4j
public class RatingMessageProducer {
    // 获取单例生产者
    Producer<String, String> producer = KafkaSingletonProducer.getInstance();

    /**
     * 发送评分消息
     * @param topic Kafka主题
     * @param uid 用户ID
     * @param mid 商品/内容ID
     * @param rating 评分值（如4.5）
     * @param timestamp 时间戳（秒级）
     */
    public void sendRatingMessage(String topic, String uid, String mid, double rating, Integer timestamp) {
        // 拼接消息内容：uid|mid|rating|timestamp
        String value = String.format("%s|%s|%.1f|%d", uid, mid, rating, timestamp);
        
        // 构造消息记录（Key设为null，由Kafka自动分区）
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, value);
        
        try {
            // 同步发送（阻塞直到收到ACK）
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("消息发送失败: Topic=%s, Value=%s, Error=%s%n",
                                      topic, value, exception.getMessage());
                } else {
                   log.info("发送成功 → Topic:{} Partition:{} Offset:{}",
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset());
                }
            }).get(); // get()确保同步等待[3](@ref)
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("消息发送中断: " + e.getMessage(), e);
        }
    }

    // 关闭生产者（需在应用退出时调用）
    public void close() {
        producer.close();
    }

}