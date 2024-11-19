package org.example.logintojwt.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "아이디를 입력하십시오")
    @Size(min = 6,max = 20,message = "아이디는 6자 이상 20자 이하입니다")
    private String username;

    @NotBlank(message = "비밀번호를 입력하십시오")
    @Size(min = 6,max = 20,message = "비밀번호는 6자 이상 20자 이하입니다")
    private String password;
}
