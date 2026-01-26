package com.szu.mallsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO {
    private Long id;
    private String code;
    private String name;
    private String remark;
    private List<String> permissionCodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
