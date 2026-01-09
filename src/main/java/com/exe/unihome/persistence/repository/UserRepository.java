package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.identityAndAuth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<User> getByEmail(String email);

  boolean existsByPhone(String phone);

  Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

}
