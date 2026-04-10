package com.github.listen_to_me.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long audioId;

    @TableField(value = "parent_id")
    private Long parentId; // 0 表示顶评论

    private Long userId;

    private String content;

    private Long likeCount;

    private LocalDateTime createTime;
}
