package com.szu.mallsystem.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "角色编码不能为空")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String remark;
}
