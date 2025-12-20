package com.medical.research.util;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/18 19:44
 * @Description:
 */
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUserUtil {

    /**
     * 获取当前登录用户的用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("未获取到登录用户信息");
        }
        // Principal 可能是 String（匿名用户）或 UserDetails（认证用户）
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("未获取到登录用户信息");
        }
        // Principal 可能是 String（匿名用户）或 UserDetails（认证用户）
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return Long.parseLong(((UserDetails) principal).getUsername().split("_")[0]);
        }
        return Long.parseLong(principal.toString().split("_")[0]);
    }

    /**
     * 获取当前登录用户的 UserDetails 对象
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("未获取到登录用户信息");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        throw new RuntimeException("当前用户信息格式异常");
    }

    /**
     * 获取 Authentication 对象（上下文核心）
     */
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}