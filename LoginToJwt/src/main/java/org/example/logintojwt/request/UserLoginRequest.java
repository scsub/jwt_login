package org.example.logintojwt.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true) // 상위 클래스의 필드도 equals, hashcode에 포함
@SuperBuilder
@NoArgsConstructor
public class UserLoginRequest extends BaseUserRequest{


}
