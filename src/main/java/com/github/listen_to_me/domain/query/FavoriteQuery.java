package com.github.listen_to_me.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询参数
 * 继承 PageQuery，自动拥有分页+排序能力
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户分页查询参数")
public class FavoriteQuery extends PageQuery {
    @Schema(description = "收藏夹ID")
    private Long folderId;
}
