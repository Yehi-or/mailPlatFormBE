package com.bsh.mailplatformmainservice.dto;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaPostDTO {
    private Long postId;
    private String channelId;
    private String message;
    private String title;
    private UUID uuid;
}
