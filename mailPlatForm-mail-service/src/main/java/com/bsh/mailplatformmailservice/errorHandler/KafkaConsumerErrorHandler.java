package com.bsh.mailplatformmailservice.errorHandler;

import com.bsh.mailplatformmailservice.dto.KafkaPostDTO;
import com.bsh.mailplatformmailservice.dto.KafkaSubDTO;
import com.bsh.mailplatformmailservice.service.KafkaConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class KafkaConsumerErrorHandler {
    private final KafkaConsumerService kafkaConsumerService;

//    public void postSubProcessDltMessage(ConsumerRecord<String, Object> record,
//                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionId,
//                                      @Header(KafkaHeaders.OFFSET) Long offset,
//                                      @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage,
//                                      @Header(KafkaHeaders.GROUP_ID) String groupId) {
//
//        KafkaSubDTO kafkaSubDTO = (KafkaSubDTO) record.value();
//
//        log.error("[SUB DLT Log] sub id ='{}' with partitionId='{}', offset='{}', topic='{}', groupId='{}'", kafkaSubDTO.getSubId(), partitionId, offset, topic, groupId);
//        kafkaConsumerService.saveFailedSubMessage(topic, partitionId, offset, errorMessage, groupId, kafkaSubDTO);
//    }

//    public void postMailProcessDltMessage(ConsumerRecord<String, Object> record,
//                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//                                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partitionId,
//                                         @Header(KafkaHeaders.OFFSET) Long offset,
//                                         @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage,
//                                         @Header(KafkaHeaders.GROUP_ID) String groupId) {
//
//        KafkaPostDTO kafkaPostDTO = (KafkaPostDTO) record.value();
//
//        log.error("[MAIL DLT Log] channel id ='{}' with partitionId='{}', offset='{}', topic='{}', groupId='{}'", kafkaPostDTO.getChannelId(), partitionId, offset, topic, groupId);
//        kafkaConsumerService.saveFailedMailMessage(topic, partitionId, offset, errorMessage, groupId, kafkaPostDTO, true);
//    }

    public void mailHandleError(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment, KafkaTemplate<String, Object> kafkaTemplate, String topic
                                       ,int partitionId, Long offset, String ExceptionName, String errorMessage, String groupId, KafkaPostDTO kafkaPostDTO) {

        try {
            //kafka consumer error 일 때
            log.info("Send to dead letter topic {} - value: {}, Exception Name : {}.", topic, kafkaPostDTO, ExceptionName);
            kafkaTemplate.send("dlt_mail_send", kafkaPostDTO);
        } catch (Exception e) {
            //db 저장
            log.error("Fail to dead letter topic {}" , topic, e);
            kafkaConsumerService.saveFailedMailMessage(topic, partitionId, offset, errorMessage, groupId, kafkaPostDTO, false);
        }

        if (Objects.nonNull(acknowledgment)) {
            acknowledgment.acknowledge();
        }
    }

    public void subHandleError(KafkaSubDTO kafkaSubDTO, String topic, int partitionId, Long offset, String groupId, String message) {
            kafkaConsumerService.saveFailedSubMessage(kafkaSubDTO, topic, partitionId, offset, groupId, message);
    }


}
