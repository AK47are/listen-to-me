package com.github.listen_to_me.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("creator_profile")
public class CreatorProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String intro;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
