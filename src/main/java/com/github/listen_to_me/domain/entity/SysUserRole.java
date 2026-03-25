package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("sys_user_role")
@Schema(name = "SysUserRole", description = "")
public class SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.INPUT)
    private Long userId;

    private Long roleId;
}
