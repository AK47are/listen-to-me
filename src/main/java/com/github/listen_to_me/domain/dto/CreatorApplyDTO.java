package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatorApplyDTO {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    @NotBlank(message = "联系电话不能为空")
    private String phone;
    private String intro;
    private String attachment;
}
