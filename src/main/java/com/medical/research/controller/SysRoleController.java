package com.medical.research.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.research.dto.sys.SysRoleReqDTO;
import com.medical.research.dto.sys.SysRoleRespDTO;
import com.medical.research.entity.SysRole;
import com.medical.research.service.SysRoleService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 系统角色控制器
 */
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
@Tag(name = "系统角色模块", description = "角色管理（仅管理员可操作）")
public class SysRoleController {
    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "分页查询角色",
            parameters = {
                    @Parameter(name = "pageNum", description = "页码", required = true, example = "1"),
                    @Parameter(name = "pageSize", description = "页大小", required = true, example = "10"),
                    @Parameter(name = "roleName", description = "角色名称（模糊）", example = "管理员"),
                    @Parameter(name = "roleCode", description = "角色编码（模糊）", example = "admin")
            }
    )
    public Result<IPage<SysRoleRespDTO>> getRolePage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode) {

        SysRoleReqDTO req = new SysRoleReqDTO();
        req.setRoleName(roleName);
        req.setRoleCode(roleCode);
        req.setPageNum(pageNum);
        req.setPageSize(pageSize);

        IPage<SysRoleRespDTO> result = sysRoleService.getRolePage(req);
        return Result.success("查询成功", result);
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "查询所有角色", description = "返回角色列表（无分页）")
    public Result<List<SysRole>> getRoleList() {
        List<SysRole> list = sysRoleService.list();
        return Result.success("查询成功", list);
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "查询角色详情",
            parameters = {@Parameter(name = "id", description = "角色ID", required = true, example = "1")}
    )
    public Result<SysRole> getRoleById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success("查询成功", role);
    }

    /**
     * 新增角色
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "新增系统角色", description = "创建角色并配置编码和描述")
    public Result<?> createRole(@Valid @RequestBody SysRoleReqDTO roleDTO) {
        // 检查角色编码是否重复
        if (sysRoleService.getByRoleCode(roleDTO.getRoleCode()) != null) {
            return Result.error("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(roleDTO.getRoleName());
        role.setRoleCode(roleDTO.getRoleCode());
        role.setDescription(roleDTO.getDescription());
        boolean success = sysRoleService.save(role);
        return success ? Result.success("角色创建成功") : Result.error("角色创建失败");
    }

    /**
     * 修改角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "修改角色信息",
            parameters = {@Parameter(name = "id", description = "角色ID", required = true, example = "1")}
    )
    public Result<?> updateRole(@PathVariable Long id, @Valid @RequestBody SysRoleReqDTO roleDTO) {
        SysRole role = sysRoleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        // 检查角色编码是否重复（排除自身）
        SysRole existRole = sysRoleService.getByRoleCode(roleDTO.getRoleCode());
        if (existRole != null && !existRole.getId().equals(id)) {
            return Result.error("角色编码已存在");
        }

        role.setRoleName(roleDTO.getRoleName());
        role.setRoleCode(roleDTO.getRoleCode());
        role.setDescription(roleDTO.getDescription());
        boolean success = sysRoleService.updateById(role);
        return success ? Result.success("角色修改成功") : Result.error("角色修改失败");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "删除角色",
            parameters = {@Parameter(name = "id", description = "角色ID", required = true, example = "3")}
    )
    public Result<?> deleteRole(@PathVariable Long id) {
        // 禁止删除管理员和科研人员角色
        if (id == 1 || id == 2) {
            return Result.error("禁止删除系统内置角色");
        }
        // 检查角色是否被用户关联
        if (sysRoleService.hasUserAssociated(id)) {
            return Result.error("该角色已分配给用户，无法删除");
        }
        boolean success = sysRoleService.removeById(id);
        return success ? Result.success("角色删除成功") : Result.error("角色删除失败");
    }
}