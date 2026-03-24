package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("audio_tag_relation")
public class AudioTagRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long audioId;

    private Long tagId;
}
