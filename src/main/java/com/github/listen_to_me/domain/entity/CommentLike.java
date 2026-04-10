package com.github.listen_to_me.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("comment_likes")
public class CommentLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;

    private Long userId;

    private LocalDateTime createTime;
}
