package com.szu.mallsystem.dto.permission;

import lombok.Data;

@Data
public class UpdatePermissionRequest {
    private String name;
    private Integer type;
    private String httpMethod;
    private String httpPath;
}
