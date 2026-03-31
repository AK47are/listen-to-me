package com.github.listen_to_me.common.producer;

import cn.hutool.json.JSONUtil;
import com.github.listen_to_me.common.config.AudioTranscodeMqConfig;
import com.github.listen_to_me.domain.dto.TranscodeTaskDTO;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 音频转码任务生产者
 */
@Component
public class AudioTranscodeProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送转码任务
     * @param audioId 音频ID
     * @param objectName MinIO存储的原始文件名/路径
     * @param trialDuration 截取前 trialDuration 秒
     */
    public void sendTranscodeTask(Long audioId, String objectName, Integer trialDuration) {
        // 直接发送 JSON 字符串
        HashMap<String, Object> map = new HashMap<>();
        map.put("audioId", audioId);
        map.put("objectName", objectName);
        map.put("trialDuration", trialDuration);
        String json = JSONUtil.toJsonStr(map);
        rabbitTemplate.convertAndSend(
                AudioTranscodeMqConfig.EXCHANGE_NAME,
                AudioTranscodeMqConfig.ROUTING_KEY,
                json
        );
    }
}