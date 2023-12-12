package com.bsh.mailplatformmainservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class CancelSubscriptionDTO {
    private Long subId;
    private Long channelId;
    private UUID uuid;
}
