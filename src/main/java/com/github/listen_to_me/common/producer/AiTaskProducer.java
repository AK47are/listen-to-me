package com.github.listen_to_me.common.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.github.listen_to_me.common.config.AiTaskMqConfig;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AiTaskProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendTranscriptionTask(String taskId, Long audioId) {
        String json = JSONUtil.createObj()
                .set("taskId", taskId)
                .set("audioId", audioId)
                .toString();
        rabbitTemplate.convertAndSend(
                AiTaskMqConfig.EXCHANGE_NAME,
                AiTaskMqConfig.TRANSCRIPTION_ROUTING_KEY,
                json);
        log.debug("发送转写任务 - taskId: {}, audioId: {}", taskId, audioId);
    }
}
