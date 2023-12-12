package com.bsh.mailplatformmailservice.dto;

import com.bsh.mailplatformmailservice.entity.KafkaConsumerMailError;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class KafkaMailErrorDTO {

    private int partitionId;

    private Long offset;

    private String errorMessage;

    private String channelId;

    private String uuid;

    private String message;
    private String title;

    private String topic;

    private String groupId;
    private boolean isDlt;

    @Builder
    public KafkaMailErrorDTO(int partitionId, Long offset, String errorMessage, String channelId, String uuid, String topic, String groupId, String message, String title, boolean isDlt) {
        this.partitionId = partitionId;
        this.offset = offset;
        this.errorMessage = errorMessage;
        this.channelId = channelId;
        this.uuid = uuid;
        this.topic = topic;
        this.groupId = groupId;
        this.message = message;
        this.title = title;
        this.isDlt = isDlt;
    }

    public KafkaConsumerMailError toEntity() {
        return KafkaConsumerMailError.builder()
                .errorMessage(errorMessage)
                .channelId(channelId)
                .uuid(uuid)
                .offsetNumber(offset)
                .partitionId(partitionId)
                .message(message)
                .groupId(groupId)
                .title(title)
                .build();
    }
}
