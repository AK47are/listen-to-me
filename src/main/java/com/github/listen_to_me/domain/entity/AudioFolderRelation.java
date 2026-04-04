package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("audio_folder_relation")
@Schema(name = "AudioFolderRelation", description = "音频收藏夹关联")
public class AudioFolderRelation {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long audioId;
    private Long folderId;
}
