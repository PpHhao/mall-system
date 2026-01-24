package com.szu.mallsystem.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAddressRequest {
    @NotBlank(message = "receiverName is required")
    @Size(max = 50, message = "receiverName length must be <= 50")
    private String receiverName;

    @NotBlank(message = "receiverPhone is required")
    @Size(max = 20, message = "receiverPhone length must be <= 20")
    private String receiverPhone;

    @NotBlank(message = "province is required")
    @Size(max = 50, message = "province length must be <= 50")
    private String province;

    @NotBlank(message = "city is required")
    @Size(max = 50, message = "city length must be <= 50")
    private String city;

    @NotBlank(message = "district is required")
    @Size(max = 50, message = "district length must be <= 50")
    private String district;

    @NotBlank(message = "detail is required")
    @Size(max = 255, message = "detail length must be <= 255")
    private String detail;

    @Size(max = 20, message = "postalCode length must be <= 20")
    private String postalCode;

    private Integer isDefault;
}
