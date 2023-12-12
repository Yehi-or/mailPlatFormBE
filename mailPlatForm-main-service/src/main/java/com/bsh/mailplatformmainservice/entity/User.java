package com.bsh.mailplatformmainservice.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class User {
    public static final String DEFAULT_IMAGE_URL = "";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String nickname;

    @Column(length = 1024)
    private String imageUrl;

    @Column
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String oauthId;

    @OneToMany(mappedBy = "sub", fetch = FetchType.LAZY)
    private List<Subscribe> subscribes = new ArrayList<>();

    @Builder
    public User(String nickname, Provider provider, String oauthId, String imageUrl, String userEmail) {
        this.nickname = nickname;
        this.userEmail = userEmail;
        this.provider = provider;
        this.oauthId = oauthId;
        this.imageUrl = imageUrl;
    }

    public void updateProfile(String nickname, String imageUrl) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }
}
