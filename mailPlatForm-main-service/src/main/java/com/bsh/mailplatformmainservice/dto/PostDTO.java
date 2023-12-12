package com.bsh.mailplatformmainservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@Builder
public class PostDTO {
    private String html;
    private Long userId;
    private Date date;
    private String title;
    private UUID uuid;
}
