package com.bsh.mailplatformmainservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ReturnMailDTO {
    private Long subId;
    private Long channelId;
    private UUID uuid;
}
