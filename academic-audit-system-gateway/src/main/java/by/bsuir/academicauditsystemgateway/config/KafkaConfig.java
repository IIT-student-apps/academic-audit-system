package by.bsuir.academicauditsystemgateway.config;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaTemplate<String, DocumentAnalyzeRequestDto> kafkaTemplate(ProducerFactory<String, DocumentAnalyzeRequestDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
