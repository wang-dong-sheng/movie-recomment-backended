package pqdong.movie.recommend.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import java.util.Properties;

/**
 * kafka连接设置，创建
 * 单例模式
 */
public class KafkaSingletonProducer {
    // 使用 volatile + 双重检查锁保证线程安全[6,8](@ref)
    private static volatile Producer<String, String> instance;

    // 私有构造方法，防止外部实例化
    private KafkaSingletonProducer() {}

    public static Producer<String, String> getInstance() {
        if (instance == null) {
            synchronized (KafkaSingletonProducer.class) {
                if (instance == null) {
                    Properties props = new Properties();
                    props.put("bootstrap.servers", "localhost:9092");
                    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                    props.put("acks", "all"); // 最高可靠性[4](@ref)
                    props.put("retries", 3); // 失败重试[3](@ref)
                    instance = new KafkaProducer<>(props);
                }
            }
        }
        return instance;
    }

    // JVM 关闭时自动清理资源
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.close();
            }
        }));
    }
}