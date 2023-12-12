package com.bsh.mailplatformmailservice.entity;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = @Index(name = "idx_uuid", columnList = "uuid"))
public class DltDupProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailErrorId;

    @Column(unique = true, name = "uuid")
    @NotNull
    private String uuid;

}
