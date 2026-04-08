package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FolderDTO {

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 50, message = "收藏夹名称长度不能超过50")
    private String name;

    @Size(max = 255, message = "描述长度不能超过255")
    private String description;
}
