package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user_folder")
@Schema(name = "SysUserFolder", description = "用户收藏夹")
public class SysUserFolder {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.INPUT)
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "收藏夹ID")
    private Long folderId;
}
