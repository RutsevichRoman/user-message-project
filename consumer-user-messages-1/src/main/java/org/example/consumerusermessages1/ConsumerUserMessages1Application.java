package org.example.consumerusermessages1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@ComponentScan(basePackages = {
    "org.example",        // ← Пакет стартера
})
public class ConsumerUserMessages1Application {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerUserMessages1Application.class, args);
    }

}
