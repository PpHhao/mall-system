package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private List<String> roles;
    private List<String> permissions;
}
