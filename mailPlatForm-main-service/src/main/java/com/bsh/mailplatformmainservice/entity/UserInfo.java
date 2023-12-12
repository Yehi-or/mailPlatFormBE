package com.bsh.mailplatformmainservice.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userInfoId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column
    private String userComment;

    @Column
    private String infoType;

    @Builder
    public UserInfo(User userId, String userComment, String infoType) {
        this.userId = userId;
        this.userComment = userComment;
        this.infoType = infoType;
    }

    public void updateUserInfo(String userComment, String infoType) {
        this.userComment = userComment;
        this.infoType = infoType;
    }
}
