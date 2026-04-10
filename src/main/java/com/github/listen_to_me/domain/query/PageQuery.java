package com.github.listen_to_me.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "分页查询基础参数")
public class PageQuery {

    /**
     * 当前页码，从 1 开始
     */
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "当前页码[≥1]", defaultValue = "1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数不能小于1")
    @Schema(description = "每页条数[≥1]", defaultValue = "10")
    private Integer pageSize = 10;

}
