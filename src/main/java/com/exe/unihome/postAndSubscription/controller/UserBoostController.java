package com.exe.unihome.postAndSubscription.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.subscription.request.CreateUserBoostRequest;
import com.exe.unihome.dto.subscription.response.UserBoostResponse;
import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
import com.exe.unihome.postAndSubscription.UserBoostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-boosts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Boost", description = "User boost management APIs")
public class UserBoostController {

  private final UserBoostService userBoostService;

  @PostMapping
  public ResponseEntity<UserBoostResponse> createUserBoost(@Valid @RequestBody CreateUserBoostRequest request) {
    log.info("Create user boost request received for user: {}", request.getUserId());
    UserBoostResponse response = userBoostService.createUserBoost(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserBoostResponse> getUserBoostById(@PathVariable String id) {
    log.info("Get user boost by ID: {}", id);
    UserBoostResponse response = userBoostService.getUserBoostById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<UserBoostResponse>> getUserBoostsByUserId(@PathVariable String userId) {
    log.info("Get user boosts for user: {}", userId);
    List<UserBoostResponse> responses = userBoostService.getUserBoostsByUserId(userId);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/user/{userId}/paginated")
  public ResponseEntity<Page<UserBoostResponse>> getUserBoostsByUserIdPaginated(
    @PathVariable String userId,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Get user boosts for user: {} with pagination", userId);
    Page<UserBoostResponse> responses = userBoostService.getUserBoostsByUserId(userId, pageable);
    return ResponseEntity.ok(responses);
  }

  @GetMapping
  public ResponseEntity<Page<UserBoostResponse>> getAllUserBoosts(
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Get all user boosts with pagination");
    Page<UserBoostResponse> responses = userBoostService.getAllUserBoosts(pageable);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<Page<UserBoostResponse>> getUserBoostsByStatus(
    @PathVariable String status,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Get user boosts by status: {}", status);
    Page<UserBoostResponse> responses = userBoostService.getUserBoostsByStatus(status, pageable);
    return ResponseEntity.ok(responses);
  }


  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse> cancelUserBoost(@PathVariable String id, Authentication authentication) {
    log.info("Delete user boost: {}", id);
    String userId = authentication.getName();
    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    userBoostService.deleteUserBoost(id, userId, isAdmin);
    return ResponseEntity.ok(
      ApiResponse.builder()
        .code(200)
        .message("Cancel Successfully")
        .build()
    );
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResponse> updateUserBoostStatus(@PathVariable String id, @RequestBody UserBoostStatus status) {
    log.info("Delete user boost: {}", id);
    userBoostService.updateUserBoostStatus(id, status);
    return ResponseEntity.ok(
      ApiResponse.builder()
        .code(200)
        .message("Cancel Successfully")
        .build()
    );
  }

}

