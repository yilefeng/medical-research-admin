package com.medical.research.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.research.dto.sys.SysUserReqDTO;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.medical.research.dto.sys.SysUserRoleReqDTO;
import com.medical.research.dto.sys.SysUserRoleRespDTO;
import com.medical.research.entity.sys.SysUser;
import com.medical.research.service.SysUserRoleService;
import com.medical.research.service.SysUserService;
import com.medical.research.util.PasswordUtil;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户控制器
 */
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
@Tag(name = "系统用户模块", description = "用户管理（仅管理员可操作）")
public class SysUserController {
    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;

    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "分页查询用户",
            parameters = {
                    @Parameter(name = "pageNum", description = "页码", required = true, example = "1"),
                    @Parameter(name = "pageSize", description = "页大小", required = true, example = "10"),
                    @Parameter(name = "username", description = "用户名（模糊）", example = "admin"),
                    @Parameter(name = "realName", description = "真实姓名（模糊）", example = "张"),
                    @Parameter(name = "status", description = "状态（1-启用，0-禁用）", example = "1")
            }
    )
    public Result<IPage<SysUserRespDTO>> getUserPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        SysUserReqDTO req = new SysUserReqDTO();
        req.setUsername(username);
        req.setRealName(realName);
        req.setStatus(status);
        req.setPageNum(pageNum);
        req.setPageSize(pageSize);
        IPage<SysUserRespDTO> result = sysUserService.getUserPage(req);
        return Result.success("查询成功", result);
    }

    /**
     * 查询用户
     */
    @GetMapping("/list")
    @Operation(
            summary = "查询用户"
    )
    public Result<List<SysUserRespDTO>> getUserList() {
        List<SysUser> list = sysUserService.list(new QueryWrapper<>());
        List<SysUserRespDTO> result = list.stream().map(user -> {
            SysUserRespDTO sysUserRespDTO = new SysUserRespDTO();
            sysUserRespDTO.setId(user.getId());
            sysUserRespDTO.setUsername(user.getUsername());
            return sysUserRespDTO;
        }).collect(Collectors.toList());
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "查询用户详情",
            parameters = {@Parameter(name = "id", description = "用户ID", required = true, example = "1")}
    )
    public Result<SysUserRespDTO> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        SysUserRespDTO sysUserRespDTO = new SysUserRespDTO();
        BeanUtils.copyProperties(user, sysUserRespDTO);
        List<SysUserRoleRespDTO> rolesByUserId = sysUserRoleService.getRolesByUserId(user.getId());
        if (rolesByUserId != null) {
            sysUserRespDTO.setRoleId(rolesByUserId.get(0).getRoleId());
        }
        return Result.success("查询成功", sysUserRespDTO);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "新增系统用户", description = "创建用户并分配角色，初始密码123456")
    public Result<?> createUser(@Valid @RequestBody SysUserReqDTO userDTO) {
        // 1. 检查用户名是否重复
        if (sysUserService.getUserByUsername(userDTO.getUsername()) != null) {
            return Result.error("用户名已存在");
        }

        // 2. 创建用户（密码默认123456，MD5加密）
        SysUser user = new SysUser();
        user.setUsername(userDTO.getUsername());
        user.setPassword(PasswordUtil.encrypt("123456"));
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setStatus(1); // 默认启用
        boolean success = sysUserService.save(user);

        // 3. 分配角色
        if (success && userDTO.getRoleId() != null) {
            SysUserRoleReqDTO req = new SysUserRoleReqDTO();
            req.setUserId(user.getId());
            req.setRoleIds(Collections.singletonList(userDTO.getRoleId()));
            sysUserRoleService.assignRoles(req);
        }

        return success ? Result.success("用户创建成功，初始密码：123456") : Result.error("用户创建失败");
    }

    /**
     * 修改用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "修改用户信息",
            parameters = {@Parameter(name = "id", description = "用户ID", required = true, example = "1")}
    )
    public Result<?> updateUser(@PathVariable Long id, @Valid @RequestBody SysUserReqDTO userDTO) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setStatus(userDTO.getStatus());
        boolean success = sysUserService.updateById(user);

        // 更新角色
        if (success && userDTO.getRoleId() != null) {
            SysUserRoleReqDTO req = new SysUserRoleReqDTO();
            req.setUserId(id);
            req.setRoleIds(Collections.singletonList(userDTO.getRoleId()));
            sysUserRoleService.assignRoles(req);
        }

        return success ? Result.success("用户修改成功") : Result.error("用户修改失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "删除用户",
            parameters = {@Parameter(name = "id", description = "用户ID", required = true, example = "2")}
    )
    public Result<?> deleteUser(@PathVariable Long id) {
        // 禁止删除管理员
        if (id == 1) {
            return Result.error("禁止删除管理员账户");
        }
        // 删除用户及关联角色
        sysUserRoleService.deleteByUserId(id);
        boolean success = sysUserService.removeById(id);
        return success ? Result.success("用户删除成功") : Result.error("用户删除失败");
    }

    /**
     * 重置密码
     */
    @PutMapping("/reset/password/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "重置用户密码",
            description = "重置为初始密码123456",
            parameters = {@Parameter(name = "id", description = "用户ID", required = true, example = "2")}
    )
    public Result<?> resetPassword(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(PasswordUtil.encrypt("123456"));
        boolean success = sysUserService.updateById(user);
        return success ? Result.success("密码重置成功，新密码：123456") : Result.error("密码重置失败");
    }
}