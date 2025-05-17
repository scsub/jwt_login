package org.example.logintojwt.repository;

import org.example.logintojwt.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByIdAndCart_User_Id(Long cartItemId, Long userId);
}
