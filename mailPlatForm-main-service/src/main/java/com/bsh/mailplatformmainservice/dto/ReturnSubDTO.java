package com.bsh.mailplatformmainservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReturnSubDTO {
    private boolean successSub;
    private String returnMessage;
    private String subscriptionId;
    private boolean lock;
}
