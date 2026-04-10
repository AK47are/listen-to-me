package com.github.listen_to_me.domain.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author baomidou
 * @since 2026-03-25
 */
@Getter
@Setter
@TableName("sys_role_permission")
@Schema(name = "SysRolePermission", description = "")
public class SysRolePermission implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.INPUT)
    private Long roleId;

    private Long permId;
}
