package com.bsh.mailplatformmailservice.config;

import com.bsh.mailplatformmailservice.dto.KafkaPostDTO;
import com.bsh.mailplatformmailservice.dto.KafkaSubDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /*
       기본적으로 kafka 는 특정 topic 을 같은 그룹 내에서 파티션으로 나눠서 처리를 한다.
       보통적으로는 하나의 서비스작업은 하나의 그룹으로 묶는다.
       만약 다른 그룹으로 사용하고 싶은 topic 이 있다면 KafkaListener 에서 groupId 를 변경해준다.
       그리고 Bean 으로 새로운 그룹을 설정 해주거나 properties 를 추가한다.

       sub 과 dlt sub 이 그룹 아이디가 달라야할까??
    */

    /**
     * mail Dlt consumer Factory
     * **/

    @Value("${KAFKA_BOOTSTRAP_SERVER}")
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
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-mail-group");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer, errorHandlingDeserializer.isForKey());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaPostDTO> kafkaListenerMailDltContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaPostDTO> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerMailDltFactory());
        kafkaListenerContainerFactory.setCommonErrorHandler(customErrorHandler());
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return kafkaListenerContainerFactory;
    }

    /**
     * subscription Dlt consumer Factory
     * **/

    @Bean
    public ConsumerFactory<String, KafkaSubDTO> consumerSubDltFactory() {
        Map<String, Object> properties = new HashMap<>();

        JsonDeserializer<KafkaSubDTO> deserializer = new JsonDeserializer<>(KafkaSubDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        ErrorHandlingDeserializer<KafkaSubDTO> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(deserializer);

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-sub-group");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer, errorHandlingDeserializer.isForKey());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaSubDTO> kafkaListenerSubDltContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaSubDTO> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerSubDltFactory());
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return kafkaListenerContainerFactory;
    }

    private DefaultErrorHandler customErrorHandler(){
        DefaultErrorHandler errorHandler = new DefaultErrorHandler();
        errorHandler.addNotRetryableExceptions(Exception.class); // retry X
        return errorHandler;
    }

    /**
     * subscription consumer Factory
     * **/

    @Bean
    public ConsumerFactory<String, KafkaSubDTO> consumerSubscriptionFactory() {
        Map<String, Object> properties = new HashMap<>();

        JsonDeserializer<KafkaSubDTO> deserializer = new JsonDeserializer<>(KafkaSubDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        ErrorHandlingDeserializer<KafkaSubDTO> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(deserializer);

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "subscription-group");
        properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer, errorHandlingDeserializer.isForKey());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaSubDTO> kafkaListenerSubContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaSubDTO> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerSubscriptionFactory());
        kafkaListenerContainerFactory.setCommonErrorHandler(subErrorHandler(kafkaTemplate));
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return kafkaListenerContainerFactory;
    }

    private DefaultErrorHandler subErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer dlqRecover = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, e) -> new TopicPartition(record.topic() + ".dlc", record.partition()));
        return new DefaultErrorHandler(dlqRecover, new FixedBackOff(0L, 3L));
    }

    /**
     * mail consumer Factory
     * **/

    @Bean
    public ConsumerFactory<String, KafkaPostDTO> consumerMailFactory() { //접속하고자 하는 정보 topic
        Map<String, Object> properties = new HashMap<>();

        JsonDeserializer<KafkaPostDTO> deserializer = new JsonDeserializer<>(KafkaPostDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        ErrorHandlingDeserializer<KafkaPostDTO> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(deserializer);

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "mail-group");
        properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer, errorHandlingDeserializer.isForKey());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaPostDTO> kafkaListenerMailContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaPostDTO> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerMailFactory());
//        kafkaListenerContainerFactory.setCommonErrorHandler(mailErrorHandler(kafkaTemplate));
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return kafkaListenerContainerFactory;
    }

/*
    private DefaultErrorHandler mailErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer dlqRecover = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, e) -> new TopicPartition(record.topic() + ".dlc", record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(dlqRecover, new FixedBackOff(0L, 3L));
        //retry 안할 에러
        errorHandler.addNotRetryableExceptions(JobExecutionAlreadyRunningException.class);
        errorHandler.addNotRetryableExceptions(JobInstanceAlreadyCompleteException.class);
        errorHandler.addNotRetryableExceptions(JobParametersInvalidException.class);
        return errorHandler;
    }
*/

    @Bean
    public ConsumerFactory<String, KafkaSubDTO> consumerCancelSubscriptionFactory() {
        Map<String, Object> properties = new HashMap<>();

        JsonDeserializer<KafkaSubDTO> deserializer = new JsonDeserializer<>(KafkaSubDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        ErrorHandlingDeserializer<KafkaSubDTO> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(deserializer);

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_server);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "cancel-subscription-group");
        properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer, errorHandlingDeserializer.isForKey());
    }

    @Bean //접속 정보를 가지고 리스너2 생성
    public ConcurrentKafkaListenerContainerFactory<String, KafkaSubDTO> kafkaListenerCancelSubContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaSubDTO> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerCancelSubscriptionFactory());
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return kafkaListenerContainerFactory;
    }

    @Bean
    public JsonMessageConverter jsonMessageConverter() {
        return new JsonMessageConverter();
    }
}
