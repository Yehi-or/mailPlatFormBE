package com.bsh.mailplatformmailservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KafkaConsumerSubError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subErrorId;

    @Column
    private int partitionId;

    @Column
    private Long offsetNumber;

    @Column
    private String errorMessage;

    @Column
    private String subId;

    @Column
    private String channelId;

    @Column
    private String uuid;

    @Column
    private String topic;

    @Column
    private String groupId;
}
