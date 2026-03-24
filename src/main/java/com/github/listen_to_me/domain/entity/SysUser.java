package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author kun
 * @since 2026-03-24
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String username;

    /**
     * BCrypt加密
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 三方平台唯一标识
     */
    private String openid;

    /**
     * 0-听众, 1-创作者
     */
    private Boolean isCreator;

    /**
     * 可提现余额
     */
    private BigDecimal balance;

    /**
     * 账期内冻结金额
     */
    private BigDecimal frozenBalance;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
