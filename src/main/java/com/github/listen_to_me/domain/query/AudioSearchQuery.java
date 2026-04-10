package com.github.listen_to_me.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "音频搜索查询参数")
public class AudioSearchQuery extends PageQuery {
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 搜索类型
     * 枚举值: TITLE / CREATOR / TRANSCRIPT， 默认 TITLE
     */
    @NotBlank(message = "搜索类型不能为空")
    private String searchType = "TITLE";
}
