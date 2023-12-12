package com.bsh.mailplatformmailservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class KafkaConsumerMailError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailErrorId;

    @Column
    private int partitionId;

    @Column
    private Long offsetNumber;

    @Column
    private String errorMessage;

    @Column
    private String channelId;

    @Column
    private String groupId;

    @Column
    private String uuid;

    @Column
    private String message;

    @Column
    private String title;

    @Column
    private boolean isDlt;
}
