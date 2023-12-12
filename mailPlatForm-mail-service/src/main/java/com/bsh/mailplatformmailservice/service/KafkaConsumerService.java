package com.bsh.mailplatformmailservice.service;

import com.bsh.mailplatformmailservice.dto.KafkaPostDTO;
import com.bsh.mailplatformmailservice.dto.KafkaMailErrorDTO;
import com.bsh.mailplatformmailservice.dto.KafkaSubDTO;
import com.bsh.mailplatformmailservice.dto.KafkaSubErrorDTO;
import com.bsh.mailplatformmailservice.repository.KafkaConsumerMailErrorRepository;
import com.bsh.mailplatformmailservice.repository.KafkaConsumerSubErrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final KafkaConsumerMailErrorRepository kafkaConsumerMailError;
    private final KafkaConsumerSubErrorRepository kafkaConsumerSubError;

    public void saveFailedSubMessage(KafkaSubDTO kafkaSubDTO, String topic, int partitionId, Long offset, String errorMessage, String groupId) {

        KafkaSubErrorDTO kafkaSubErrorDTO = KafkaSubErrorDTO.builder()
                .subId(kafkaSubDTO.getSubId())
                .channelId(kafkaSubDTO.getChannelId())
                .uuid(kafkaSubDTO.getUuid().toString())
                .offset(offset)
                .groupId(groupId)
                .partitionId(partitionId)
                .errorMessage(errorMessage)
                .topic(topic)
                .build();

        kafkaConsumerSubError.save(kafkaSubErrorDTO.toEntity());
    }

    public void saveFailedMailMessage(String topic, int partitionId, Long offset, String errorMessage, String groupId, KafkaPostDTO kafkaPostDTO, boolean isDlt) {

        KafkaMailErrorDTO kafkaMailErrorDTO = KafkaMailErrorDTO.builder()
                .channelId(kafkaPostDTO.getChannelId())
                .uuid(kafkaPostDTO.getUuid().toString())
                .message(kafkaPostDTO.getMessage())
                .offset(offset)
                .partitionId(partitionId)
                .errorMessage(errorMessage)
                .groupId(groupId)
                .topic(topic)
                .isDlt(isDlt)
                .build();

        kafkaConsumerMailError.save(kafkaMailErrorDTO.toEntity());
    }
}
