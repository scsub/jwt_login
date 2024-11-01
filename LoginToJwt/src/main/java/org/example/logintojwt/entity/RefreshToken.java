package org.example.logintojwt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String token;

    private Long expiration;

    public void changeUsername(String username) {
        this.username = username;
    }

    public void changeToken(String token) {
        this.token = token;
    }

    public void changeExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
