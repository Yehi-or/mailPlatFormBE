package com.bsh.mailplatformmainservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaMailDTO {
    private String channelId;
    private UUID uuid;
}
