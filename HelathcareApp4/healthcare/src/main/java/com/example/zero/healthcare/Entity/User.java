package com.example.zero.healthcare.Entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "id_token", length = 2000)
    private String idToken;

    @Column(name = "nickname")
    private String nickname;


    public User(String email, String idToken, String nickname) {
        this.email = email;
        this.idToken = idToken;
        this.nickname = nickname;
    }
}
