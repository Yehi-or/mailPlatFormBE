package com.bsh.mailplatformmailservice.dto;

import com.bsh.mailplatformmailservice.entity.KafkaConsumerSubError;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KafkaSubErrorDTO {
    private int partitionId;
    private Long offset;
    private String errorMessage;
    private String groupId;
    private String subId;
    private String channelId;
    private String uuid;
    private String topic;

    @Builder
    public KafkaSubErrorDTO(int partitionId, Long offset, String errorMessage, String subId, String channelId, String uuid, String topic, String groupId) {
        this.partitionId = partitionId;
        this.offset = offset;
        this.errorMessage = errorMessage;
        this.subId = subId;
        this.channelId = channelId;
        this.uuid = uuid;
        this.topic = topic;
        this.groupId = groupId;
    }

    public KafkaConsumerSubError toEntity() {
        return KafkaConsumerSubError.builder()
                .subId(subId)
                .errorMessage(errorMessage)
                .channelId(channelId)
                .uuid(uuid)
                .offsetNumber(offset)
                .partitionId(partitionId)
                .groupId(groupId)
                .build();
    }

}
