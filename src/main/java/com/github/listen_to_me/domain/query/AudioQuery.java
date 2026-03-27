package com.github.listen_to_me.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AudioQuery {
    @Schema(description = "当前页", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "-1")
    private Integer pageSize = -1;

    @Schema(description = "音频标题")
    private String title;

    @Schema(description = "音频标签")
    private List<String> tags = new ArrayList<>();
}
