package com.bsh.mailplatformmainservice.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "subscribe", uniqueConstraints = {@UniqueConstraint(columnNames = {"sub_user_id", "pup_user_id"})})
public class Subscribe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_user_id", nullable = false)
    private User sub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pup_user_id", nullable = false)
    private User pub;

    @Column(nullable = false)
    private boolean notification;

    @Builder
    public Subscribe(User sub, User pub, boolean notification) {
        this.sub = sub;
        this.pub = pub;
        this.notification = notification;
    }

    public void changeNotification(boolean notification) {
        this.notification = notification;
    }
}
