package com.bsh.mailplatformmailservice.controller;

import com.bsh.mailplatformmailservice.dto.KafkaPostDTO;
import com.bsh.mailplatformmailservice.errorHandler.KafkaConsumerErrorHandler;
import com.bsh.mailplatformmailservice.service.SendJobParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class MailController {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConsumerErrorHandler kafkaConsumerErrorHandler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SendJobParameterService sendMailService;

    @KafkaListener(topics = "mail", groupId = "mail-group", containerFactory = "kafkaListenerMailContainerFactory")
    public void sendingMail(KafkaPostDTO kafkaPostDTO, ConsumerRecord<String, Object> record, Acknowledgment acknowledgment,
                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                            @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionId,
                            @Header(KafkaHeaders.OFFSET) Long offset,
                            @Header(KafkaHeaders.GROUP_ID) String groupId) {
        try {
            log.info("channel Id : {}", kafkaPostDTO.getChannelId());
            sendMailService.sendJobParameterManualCommit(kafkaPostDTO, acknowledgment, redisTemplate);
        } catch(Exception e) {
            log.error("Error name : {}, Error message : {}", e.getClass().getName(), e.toString());
            kafkaConsumerErrorHandler.mailHandleError(record, acknowledgment, kafkaTemplate, topic, partitionId, offset, e.getClass().getName(), e.toString(), groupId, kafkaPostDTO);
        }
    }

    @KafkaListener(topics = "dlt_mail_send", groupId = "dlt-mail-group", containerFactory = "kafkaListenerMailDltContainerFactory")
    public void mailDltTopic(KafkaPostDTO kafkaPostDTO, Acknowledgment acknowledgment) {
        try {
            log.info("dlt channel Id : {}", kafkaPostDTO.getChannelId());
            sendMailService.sendJobParameterManualCommit(kafkaPostDTO, acknowledgment, redisTemplate);
        } catch (Exception e) {
            log.error("redis or Batch Error retry send to main-service for save fail");
            kafkaTemplate.send("mail.dlt", kafkaPostDTO);
        }
    }

}
