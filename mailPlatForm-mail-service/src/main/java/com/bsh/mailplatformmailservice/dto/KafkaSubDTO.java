package com.bsh.mailplatformmailservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaSubDTO {
    private String subId;
    private String channelId;
    private UUID uuid;
}
