package com.bsh.mailplatformmailservice.controller;

import com.bsh.mailplatformmailservice.dto.KafkaSubDTO;
import com.bsh.mailplatformmailservice.errorHandler.KafkaConsumerErrorHandler;
import com.bsh.mailplatformmailservice.service.SaveMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class SubController {
    private final SaveMailService saveMailService;
    private final KafkaConsumerErrorHandler kafkaConsumerErrorHandler;

    @KafkaListener(topics = "subscription", groupId = "subscription-group", containerFactory = "kafkaListenerSubContainerFactory")
    public void subscription(KafkaSubDTO kafkaSubDTO, Acknowledgment acknowledgment) {
        log.info("Subscription : {}, Channel : {}", kafkaSubDTO.getSubId(), kafkaSubDTO.getChannelId());
        saveMailService.saveMailToRedis(kafkaSubDTO, acknowledgment);
    }

    @KafkaListener(topics = "cancel-subscription", groupId = "cancel-subscription-group", containerFactory = "kafkaListenerCancelSubContainerFactory")
    public void cancelSubscription(@Payload KafkaSubDTO kafkaSubDTO, Acknowledgment acknowledgment) {
        log.info("Cancel Subscription : {}, Channel : {}", kafkaSubDTO.getSubId(), kafkaSubDTO.getChannelId());
        saveMailService.cancelSubscription(kafkaSubDTO, acknowledgment);
    }

    @KafkaListener(topics = "subscription.dlt", groupId = "dlt-sub-group", containerFactory = "kafkaListenerSubDltContainerFactory")
    public void mailDltTopic(ConsumerRecord<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionId,
                             @Header(KafkaHeaders.OFFSET) Long offset,
                             @Header(KafkaHeaders.GROUP_ID) String groupId) {
        log.error("save to dead letter topic db {}" , record.value());
        kafkaConsumerErrorHandler.subHandleError( (KafkaSubDTO) record.value(), topic, partitionId, offset, groupId, "subscription");
    }
}
