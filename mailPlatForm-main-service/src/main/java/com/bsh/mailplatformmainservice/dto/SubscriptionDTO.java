package com.bsh.mailplatformmainservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class SubscriptionDTO {
    private Long subId;
    private Long channelId;
    private boolean notification;
    private UUID uuid;
}
