package org.example.logintojwt.request;

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
public class UserProfileUpdateRequest {
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
