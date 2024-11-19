package org.example.logintojwt.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessTokenAndRefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
}
