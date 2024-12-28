package org.example.logintojwt.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = "password")
public class BaseUserRequest {
    @NotBlank(message = "아이디를 입력하십시오")
    @Size(min = 6, max = 20, message = "아이디는 6자 이상 20자 이하입니다")
    private String username;

    @NotBlank(message = "비밀번호를 입력하십시오")
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하입니다")
    private String password;
}
