package org.example.logintojwt.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPasswordChangeRequest {
    private String originalPassword;
    private String newPassword;
    private String confirmNewPassword;
}
