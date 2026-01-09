package com.exe.unihome.dto.category.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private UUID categoryId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
