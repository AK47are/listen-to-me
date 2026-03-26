package com.github.listen_to_me.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.listen_to_me.domain.entity.SysUser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户安全适配器，将 SysUser 对象转换为 Security 框架所需的 UserDetails 对象
 */
@Getter
@RequiredArgsConstructor
public class SysUserAdapter implements UserDetails {

    private final SysUser sysUser;
    private final List<String> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null) {
            return Collections.emptyList();
        }
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }
}
