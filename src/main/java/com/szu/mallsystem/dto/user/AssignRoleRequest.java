package com.szu.mallsystem.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignRoleRequest {
    private Long userId;

    @NotEmpty(message = "角色编码列表不能为空")
    private List<String> roleCodes;
}
