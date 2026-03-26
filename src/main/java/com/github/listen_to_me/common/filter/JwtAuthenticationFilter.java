package com.github.listen_to_me.common.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.listen_to_me.common.util.JwtUtils;
import com.github.listen_to_me.mapper.SysUserMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SysUserMapper sysUserMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 没带 Token 直接放行给后面的过滤器（后面会报 403）
            return;
        }

        String token = authHeader.substring(7);

        if (JwtUtils.validateToken(token)) {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            // HACK: 后续添加 Redis 后，在登录时将权限存到 Redis，而不是每次请求都调用一次
            List<String> permCodeList = sysUserMapper.selectPermCodeListById(userId);
            log.debug("识别用户 - ID: {}, 权限列表: {}", userId, permCodeList);
            List<SimpleGrantedAuthority> authorities = permCodeList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
                    authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("上下文填充完成 - 用户ID: {}", userId);
        }

        filterChain.doFilter(request, response);
    }
}
