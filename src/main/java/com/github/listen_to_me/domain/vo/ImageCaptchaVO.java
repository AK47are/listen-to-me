package com.github.listen_to_me.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageCaptchaVO {
    private String uuid;
    private String img;
}
