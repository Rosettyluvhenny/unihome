package com.exe.unihome.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for system/error messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessage {

  /**
   * Error code or status code
   */
  @JsonProperty("code")
  private Integer code;

  /**
   * Human-readable message
   */
  @JsonProperty("message")
  private String message;
}

