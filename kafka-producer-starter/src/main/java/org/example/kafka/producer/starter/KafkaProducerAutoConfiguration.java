package org.example.kafka.producer.starter;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerAutoConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory(Object serializer) {
        return new DefaultKafkaProducerFactory<>(createBaseConfig(serializer));
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory(Object serializer) {
        return new DefaultKafkaProducerFactory<>(createBaseConfig(serializer));
    }

    private Map<String, Object> createBaseConfig(Object serializer) {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);

        // Producer performance tuning for transactions (exactly-once semantics)
        config.put(ProducerConfig.ACKS_CONFIG, "all");  // All replicas must acknowledge
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // Idempotent producer
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);

        // Reliability settings
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);

        return config;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaByteTemplate() {
        return new KafkaTemplate<>(producerFactory(ByteArraySerializer.class));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaStringTemplate() {
        return new KafkaTemplate<>(stringProducerFactory(StringSerializer.class));
    }
}
