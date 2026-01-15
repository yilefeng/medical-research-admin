package com.medical.research.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.medical.research.dto.LoginDTO;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.medical.research.entity.sys.SysUser;
import com.medical.research.service.SysUserService;
import com.medical.research.util.AESUtil;
import com.medical.research.util.JwtUtil;
import com.medical.research.util.PasswordUtil;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 */
@Slf4j
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@Tag(name = "登录模块", description = "用户登录、获取token接口")
public class LoginController {
    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * 用户登录接口
     */
    @PostMapping
    @Operation(
            summary = "用户登录",
            description = "用户名密码登录，返回JWT token和用户信息",
            parameters = {
                    @Parameter(name = "username", description = "用户名", required = true, example = "admin"),
                    @Parameter(name = "password", description = "密码（明文）", required = true, example = "123456")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "用户名不存在/密码错误/用户已禁用")
            }
    )
    public Result<?> login(@RequestBody LoginDTO loginDTO) throws Exception {
        // 1. 查询用户
        SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>().eq("username", loginDTO.getUsername()));
        if (user == null) {
            return Result.error("用户名不存在");
        }
        if (user.getStatus() == 0) {
            return Result.error("用户已禁用，请联系管理员");
        }
        String decrypt = AESUtil.decrypt(loginDTO.getPassword());
        // 2. 验证密码
        if (!PasswordUtil.verify(user.getUsername(), decrypt, user.getPassword())) {
            return Result.error("密码错误");
        }

        // 3. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserService.updateById(user);

        // 4. 生成JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        SysUserRespDTO userRespDTO = new SysUserRespDTO();
        BeanUtils.copyProperties(user, userRespDTO);

        userRespDTO.setRoleCode(sysUserService.getUserById(user.getId()).getRoleCode());

        // 5. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userRespDTO);
        log.info("用户{}登录成功", user.getUsername());
        return Result.success("登录成功", result);
    }
}