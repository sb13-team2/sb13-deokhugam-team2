package com.deokhugam.user.entity;

import com.deokhugam.global.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false)
    private String password;

    public static User create(String email, String nickname, String password) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.password = password;
        return user;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
