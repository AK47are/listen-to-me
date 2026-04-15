package com.github.listen_to_me.service;

import org.springframework.stereotype.Service;

import com.alibaba.dashscope.audio.qwen_asr.QwenTranscription;
import com.alibaba.dashscope.audio.qwen_asr.QwenTranscriptionParam;
import com.alibaba.dashscope.audio.qwen_asr.QwenTranscriptionQueryParam;
import com.alibaba.dashscope.audio.qwen_asr.QwenTranscriptionResult;
import com.alibaba.dashscope.utils.Constants;
import com.github.listen_to_me.common.config.DashScopeConfig;

import cn.hutool.http.HttpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final DashScopeConfig dashScopeConfig;

    /**
     * 音频转写（录音文件识别）
     *
     * @param fileUrl 公网可访问的音频文件 URL
     * @return 转写结果 JSON 字符串
     */
    public String transcribe(String fileUrl) {
        log.debug("开始音频转写 - fileUrl: {}", fileUrl);

        Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";

        QwenTranscriptionParam param = QwenTranscriptionParam.builder()
                .apiKey(dashScopeConfig.getApiKey())
                .model(dashScopeConfig.getTranscriptionModel())
                .fileUrl(fileUrl)
                .parameter("enable_itn", false)
                .parameter("enable_words", true)
                .build();

        try {
            QwenTranscription transcription = new QwenTranscription();

            QwenTranscriptionResult result = transcription.asyncCall(param);
            String taskId = result.getTaskId();
            log.debug("转写任务已提交 - taskId: {}", taskId);

            result = transcription.wait(QwenTranscriptionQueryParam.FromTranscriptionParam(param, taskId));

            String transcriptionUrl = result.getResult().getTranscriptionUrl();
            String content = HttpRequest.get(transcriptionUrl).execute().body();

            log.debug("音频转写成功");
            return content;

        } catch (Exception e) {
            log.error("音频转写失败", e);
            throw new RuntimeException("音频转写失败: " + e.getMessage(), e);
        }
    }
}
