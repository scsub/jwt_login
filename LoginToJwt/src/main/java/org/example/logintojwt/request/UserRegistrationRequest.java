package org.example.logintojwt.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true) // 상위 클래스의 필드도 equals, hashcode에 포함
@NoArgsConstructor
@SuperBuilder
public class UserRegistrationRequest extends BaseUserRequest {
    @NotBlank
    @Size(min = 10, max = 25, message = "제대로된 이메일을 입력하세요")
    private String email;

    @NotBlank
    @Size(min = 10, max = 25, message = "전화번호는 10~25자리 입니다")
    private String phoneNumber;

    @NotBlank
    @Size(min = 2, max = 40, message = "제대로된 주소를 입력하세요")
    private String address;

    @NotBlank
    @Size(min = 6,max = 20, message = "비밀번호가 다릅니다")
    private String passwordCheck;
}
