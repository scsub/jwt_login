package org.example.logintojwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "`users`")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; //Bcrypt 암호화

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private List<Role> roles;

    @Builder
    public User(String username, String password, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles != null ? roles : List.of(Role.ROLE_USER);
    }
}
