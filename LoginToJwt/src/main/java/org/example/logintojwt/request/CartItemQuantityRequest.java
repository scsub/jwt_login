package org.example.logintojwt.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemQuantityRequest {
    @Range(min = 1, max = 100, message = "최소1개 최대100개까지 주문 가능합니다")
    private Long quantity;
    private Long originalQuantity;
}
