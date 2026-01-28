package com.exe.unihome.postAndSubscription.controller;

import com.exe.unihome.dto.subscription.request.CreateBoostRequest;
import com.exe.unihome.dto.subscription.request.UpdateBoostRequest;
import com.exe.unihome.dto.subscription.response.BoostResponse;
import com.exe.unihome.persistence.entity.subscription.BoostStatus;
import com.exe.unihome.postAndSubscription.BoostService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boosts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Boost", description = "Boost management APIs")
public class BoostController {

  private final BoostService boostService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<BoostResponse> createBoost(@Valid @RequestBody CreateBoostRequest request) {
    log.info("Create boost request received");
    BoostResponse response = boostService.createBoost(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BoostResponse> getBoostById(@PathVariable String id) {
    log.info("Get boost by ID: {}", id);
    BoostResponse response = boostService.getBoostById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<Page<BoostResponse>> getAllBoosts(
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Get all boosts with pagination");
    Page<BoostResponse> responses = boostService.getAllBoosts(pageable);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<Page<BoostResponse>> getBoostsByStatus(
    @PathVariable BoostStatus status,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    )
    Pageable pageable) {
    log.info("Get boosts by status: {}", status);
    Page<BoostResponse> responses = boostService.getBoostsByStatus(status, pageable);
    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")

  public ResponseEntity<BoostResponse> updateBoost(
    @PathVariable String id,
    @Valid @RequestBody UpdateBoostRequest request) {
    log.info("Update boost: {}", id);
    BoostResponse response = boostService.updateBoost(id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteBoost(@PathVariable String id) {
    log.info("Delete boost: {}", id);
    boostService.deleteBoost(id);
    return ResponseEntity.noContent().build();
  }
}

