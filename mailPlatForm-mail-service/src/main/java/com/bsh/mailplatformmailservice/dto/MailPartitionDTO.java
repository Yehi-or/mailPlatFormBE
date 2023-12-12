package com.bsh.mailplatformmailservice.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailPartitionDTO {
    Set<Object> mailList;
    String mailTemplate;
}
