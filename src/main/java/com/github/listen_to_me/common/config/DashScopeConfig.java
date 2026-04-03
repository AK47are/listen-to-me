package com.github.listen_to_me.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.dashscope")
public class DashScopeConfig {
    private String apiKey;
    private String transcriptionModel;
    private String summarizationModel;
    private String translationModel;
    private String slotModel;
}
