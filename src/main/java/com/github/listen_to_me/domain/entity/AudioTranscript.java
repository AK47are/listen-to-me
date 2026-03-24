package com.github.listen_to_me.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("audio_transcript")
public class AudioTranscript implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long audioId;

    /**
     * AI转写文本
     */
    private String fullText;

    /**
     * AI分段标题与时间戳 [{time: 0, title: "..."}]
     */
    private String segmentJson;
}
