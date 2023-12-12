package com.bsh.mailplatformmailservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaPostDTO {
    private Long postId;
    private String channelId;
    private String title;
    private String message;
    private UUID uuid;
}
