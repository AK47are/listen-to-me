package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@TableName("sys_user")
@Schema(name = "SysUser", description = "")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "账号")
    private String username;

    @Schema(description = "BCrypt加密")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "三方平台唯一标识")
    private String openid;

    @Schema(description = "0-听众, 1-创作者")
    private Boolean isCreator;

    @Schema(description = "可提现余额")
    private BigDecimal balance;

    @Schema(description = "账期内冻结金额")
    private BigDecimal frozenBalance;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
