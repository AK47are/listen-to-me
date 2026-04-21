package com.github.listen_to_me.common.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.listen_to_me.common.enumeration.RedisKey;
import com.github.listen_to_me.common.util.JwtUtils;
import com.github.listen_to_me.common.util.RedisUtils;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.mapper.SysUserMapper;

import cn.hutool.http.HttpStatus;
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

        Claims claims = JwtUtils.validateToken(token);
        if (claims != null) {
            Long userId = Long.parseLong(claims.getSubject());

            // 检查用户状态, 不用担心效率，单表 + 索引查询超级快
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) { // 不能抛出异常，Filter 不能被 Security 框架和自己的全局异常处理器捕获
                log.debug("用户不存在 - ID: {}", userId);
                response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"data\":null,\"msg\":\"用户不存在\"}");
                return;
            }
            if ("BANNED".equals(user.getStatus())) {
                log.debug("用户已被封禁 - ID: {}", userId);
                response.setStatus(HttpStatus.HTTP_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"data\":null,\"msg\":\"账号已被封禁\"}");
                return;
            }

            List<String> permCodeList = RedisUtils.get(RedisKey.USER_PERMS, userId.toString());

            if (permCodeList == null) {
                permCodeList = sysUserMapper.selectPermCodeListById(userId);
                RedisUtils.set(RedisKey.USER_PERMS, userId.toString(), permCodeList);
                log.debug("权限缓存未命中 - 用户ID: {}", userId);
            }

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
