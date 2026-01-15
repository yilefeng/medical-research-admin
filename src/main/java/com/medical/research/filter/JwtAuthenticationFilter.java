package com.medical.research.filter;

import com.medical.research.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Token请求头名称（默认Authorization，格式：Bearer <token>）
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String NEW_TOKEN = "new-token";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String auth = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotEmpty( auth)) {
            String token = auth.replace(BEARER_PREFIX, "").trim();
            String username = null;
            log.info("Token: {}", token);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.error("Token解析失败", e);
            }

            // 验证Token并设置认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // 验证Token有效性
                if (jwtUtil.validateToken(token, userDetails)) {
                    // 判断是否需要续期
                    if (jwtUtil.isTokenNeedRenewal(token)) {
                        //生成新Token并返回给前端
                        String newToken = jwtUtil.renewToken(token);
                        //前端拿到后替换旧Token
                        response.setHeader(NEW_TOKEN, newToken);
                        response.setHeader("Access-Control-Expose-Headers", NEW_TOKEN);
                    }
                    // 创建认证Token并存入Security上下文
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

        // 3. 放行请求
        filterChain.doFilter(request, response);
    }
}
