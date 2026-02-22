package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.cart.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("select distinct c from Cart c left join fetch c.items ci left join fetch ci.furniture left join fetch ci.sku where c.user.id = :userId")
    Optional<Cart> findWithItemsByUserId(@Param("userId") String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct c from Cart c left join fetch c.items ci left join fetch ci.furniture left join fetch ci.sku where c.user.id = :userId")
    Optional<Cart> findForUpdateByUserId(@Param("userId") String userId);
}
