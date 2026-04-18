package com.github.listen_to_me.common.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.listen_to_me.common.config.AiTaskMqConfig;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.entity.AiTask;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.mapper.AiTaskMapper;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.service.AiService;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class SummarizationConsumer {

    private final AiTaskMapper aiTaskMapper;
    private final AudioInfoMapper audioInfoMapper;
    private final AiService aiService;

    @RabbitListener(queues = AiTaskMqConfig.SUMMARIZATION_QUEUE)
    public void handleSummarizationTask(String jsonMsg) {
        JSONObject taskData = JSONUtil.parseObj(jsonMsg);
        String taskId = taskData.getStr("taskId");
        Long audioId = taskData.getLong("audioId");

        log.debug("收到摘要任务 - taskId: {}, audioId: {}", taskId, audioId);

        AiTask task = aiTaskMapper.selectOne(Wrappers.<AiTask>lambdaQuery().eq(AiTask::getTaskId, taskId));
        if (task == null) {
            log.error("任务不存在 - taskId: {}", taskId);
            return;
        }

        task.setStatus("PROCESSING");
        aiTaskMapper.updateById(task);

        try {
            AudioInfo audio = audioInfoMapper.selectById(audioId);
            if (audio == null) {
                throw new RuntimeException("音频不存在");
            }

            // 生成 MinIO 公网临时 URL
            String fileUrl = MinioUtils.getPresignedUrl(audio.getRawPath());

            // 调用 AI 生成英文摘要
            String englishSummary = aiService.generateSummary(fileUrl);

            // 翻译为中文
            String chineseSummary = aiService.translateSummary(englishSummary);

            // 构建结果 JSON
            JSONObject result = JSONUtil.createObj()
                    .set("summary", chineseSummary)
                    .set("original", englishSummary);

            task.setStatus("SUCCESS");
            task.setResult(result.toString());
            aiTaskMapper.updateById(task);

            log.debug("摘要任务成功 - taskId: {}", taskId);
        } catch (Exception e) {
            log.error("摘要任务失败 - taskId: {}", taskId, e);
            task.setStatus("FAILED");
            task.setFailReason(e.getMessage());
            aiTaskMapper.updateById(task);
        }
    }
}
