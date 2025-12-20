package com.medical.research.service.impl;

import com.medical.research.entity.SysUser;
import com.medical.research.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final SysUserService sysUserService;

    /**
     * 根据用户名加载用户信息（生产环境替换为数据库查询）
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getUserByUsername(username.substring(username.indexOf("_") +1));
        return User.withUsername(user.getId() + "_" + user.getUsername())
                .password(user.getPassword())
                .roles(sysUserService.getUserById(user.getId()).getRoleCode())
                .build();
    }
}