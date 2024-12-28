package org.example.logintojwt.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "password")
public class UserProfileRequest {
    @NotBlank
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하입니다")
    private String password;

    @NotBlank
    @Size(min = 10, max = 25, message = "제대로된 이메일을 입력하세요")
    private String email;

    @NotBlank
    @Size(min = 10, max = 25, message = "전화번호는 10~25자리 입니다")
    private String phoneNumber;

    @NotBlank
    @Size(min = 2, max = 40, message = "제대로된 주소를 입력하세요")
    private String address;
}
