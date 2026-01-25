package com.exe.unihome.dto.order.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShippingInfoRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number")
    private String phone;

    @NotBlank
    private String address;

    private String note;
}
