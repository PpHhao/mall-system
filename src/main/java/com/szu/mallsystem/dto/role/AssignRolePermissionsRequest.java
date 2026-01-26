package com.szu.mallsystem.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignRolePermissionsRequest {
    @NotNull(message = "权限编码列表不能为空")
    private List<String> permissionCodes;
}
