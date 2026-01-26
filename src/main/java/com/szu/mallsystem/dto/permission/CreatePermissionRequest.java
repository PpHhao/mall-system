package com.szu.mallsystem.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank(message = "权限编码不能为空")
    private String code;

    @NotBlank(message = "权限名称不能为空")
    private String name;

    @NotNull(message = "权限类型不能为空")
    private Integer type;

    private String httpMethod;

    private String httpPath;
}
