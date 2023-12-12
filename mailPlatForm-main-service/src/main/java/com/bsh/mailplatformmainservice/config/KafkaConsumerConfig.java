package com.bsh.mailplatformmainservice.config;

import com.bsh.mailplatformmainservice.dto.KafkaPostDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    @Value("${KAFKA_SERVER}")
    String kafka_server;

    @Bean
    public ConsumerFactory<String, KafkaPostDTO> consumerMailDltFactory() {
        Map<String, Object> properties = new HashMap<>();

        JsonDeserializer<KafkaPostDTO> deserializer = new JsonDeserializer<>(KafkaPostDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        ErrorHandlingDeserializer<KafkaPostDTO> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(deserializer);

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "mail.dlt-group");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer, errorHandlingDeserializer.isForKey());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaPostDTO> kafkaListenerMailDltContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaPostDTO> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerMailDltFactory());
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return kafkaListenerContainerFactory;
    }
}
