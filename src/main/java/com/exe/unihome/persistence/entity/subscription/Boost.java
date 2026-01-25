package com.exe.unihome.persistence.entity.subscription;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "boost")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boost {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @NonNull
  String name;

  BigDecimal price;

  @Builder.Default
  int duration = 0;

  @CreatedDate
  LocalDateTime createdAt;

  @LastModifiedDate
  LocalDateTime updatedAt;

  @Enumerated(value = EnumType.STRING)
  BoostStatus status;
}
