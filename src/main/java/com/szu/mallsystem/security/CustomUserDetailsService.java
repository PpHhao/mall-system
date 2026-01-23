package com.szu.mallsystem.security;

import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new DisabledException("用户已被禁用");
        }
        List<String> roles = userService.getRoleCodes(user.getId());
        List<String> permissions = userService.getPermissionCodes(user.getId());
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(code -> authorities.add(new SimpleGrantedAuthority("ROLE_" + code)));
        permissions.forEach(code -> authorities.add(new SimpleGrantedAuthority(code)));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                true,
                true,
                true,
                true,
                authorities
        );
    }
}
