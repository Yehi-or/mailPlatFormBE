package com.bsh.mailplatformmainservice.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_user_id", nullable = false)
    private User postUser;

    @Column
    private String html;

    @Column
    private Date postDate;

    @Column
    private String title;

    @Column()
    private boolean success;

    @Builder
    public Post(User postUser, String html, Date postDate, String title, boolean success) {
        this.postDate = postDate;
        this.postUser = postUser;
        this.html = html;
        this.title = title;
        this.success = success;
    }
}
