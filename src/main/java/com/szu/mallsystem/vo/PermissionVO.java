package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionVO {
    private String code;
    private String name;
    private Integer type;
    private String httpMethod;
    private String httpPath;
}
