package com.bsh.mailplatformmailservice.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class MailRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordsId;

    @Column(name = "mail_sender")
    private Long mailSender;

    @Column
    private String content;

    @Column
    private LocalDate date;

    @Builder
    public MailRecords(Long mailSender, String content, LocalDate date) {
        this.mailSender = mailSender;
        this.content = content;
        this.date = date;
    }
}
