package com.bsh.mailplatformmainservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class SubscriptionLockSaveDTO {
    private String subName;
    private String channelName;
}
