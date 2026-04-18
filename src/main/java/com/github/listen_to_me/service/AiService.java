package com.github.listen_to_me.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.audio.qwen_asr.QwenTranscription;
import com.alibaba.dashscope.audio.qwen_asr.QwenTranscriptionParam;
import com.alibaba.dashscope.audio.qwen_asr.QwenTranscriptionQueryParam;
import com.alibaba.dashscope.audio.qwen_asr.QwenTranscriptionResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.ResponseFormat;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import com.github.listen_to_me.common.config.DashScopeConfig;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.domain.entity.AiTask;
import com.github.listen_to_me.domain.vo.SlotVO;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final DashScopeConfig dashScopeConfig;
    private final IAiTaskService aiTaskService;

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

    /**
     * AI 生成时间槽（预览）
     *
     * @param userId      当前用户ID
     * @param description 自然语言描述
     * @return 时间槽列表
     */
    public List<SlotVO> generateSlots(Long userId, String description) {
        log.debug("AI 智能排期 - 用户ID: {}, 描述: {}", userId, description);

        // 创建 AiTask 记录
        String taskId = IdUtil.fastSimpleUUID();
        AiTask task = new AiTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setType("SLOT_GENERATION");
        task.setStatus("PROCESSING");
        aiTaskService.save(task);

        try {
            String prompt = buildSlotPrompt(description);
            String response = callDashScope(prompt, dashScopeConfig.getSlotModel());
            List<SlotVO> slots = JSONUtil.toList(JSONUtil.parseArray(response), SlotVO.class);

            // 2. 更新任务状态为 SUCCESS，存储结果
            task.setStatus("SUCCESS");
            task.setResult(response);
            aiTaskService.updateById(task);

            return slots;
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setFailReason(e.getMessage());
            aiTaskService.updateById(task);
            throw e;
        }
    }

    /**
     * 构建排期提示词
     */
    private String buildSlotPrompt(String description) {
        return "你是一个智能排期助手。请根据用户的自然语言描述，生成符合要求的时间槽列表，并以纯 JSON 数组格式返回，不要包含任何其他文本或注释。\n" +
                "当前日期是：" + LocalDate.now() + "\n" +
                "用户输入：" + description + "\n" +
                "生成的 JSON 数组中的每个对象必须包含以下字段：\n" +
                "- startTime: 开始时间，格式为 'yyyy-MM-dd HH:mm:ss'\n" +
                "- endTime: 结束时间，格式为 'yyyy-MM-dd HH:mm:ss'\n" +
                "- price: 预约价格（虚拟币），必须大于0\n" +
                "- address: 预约地址，若用户未提供则留空字符串 \"\"\n" +
                "请确保 startTime 早于 endTime，且时间基于当前日期推算。输出示例：\n" +
                "[{\"startTime\":\"2026-04-21 14:00:00\",\"endTime\":\"2026-04-21 16:00:00\",\"price\":80,\"address\":\"\"}]";
    }

    /**
     * 通用 DashScope AI 调用（纯文本）
     *
     * @param prompt 提示词
     * @param model  模型名称
     * @return AI 返回的文本
     */
    private String callDashScope(String prompt, String model) {
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        ResponseFormat jsonMode = ResponseFormat.builder().type("json_object").build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(dashScopeConfig.getApiKey())
                .model(model)
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .responseFormat(jsonMode)
                .build();

        try {
            Generation gen = new Generation();
            GenerationResult result = gen.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("DashScope 调用失败 - model: {}", model, e);
            throw new BaseException(500, "AI 调用失败: " + e.getMessage());
        }
    }

    /**
     * 生成英文摘要
     *
     * @param fileUrl 公网可访问的音频文件 URL
     * @return 英文摘要文本
     */
    public String generateSummary(String fileUrl) {
        log.debug("开始生成英文摘要 - fileUrl: {}", fileUrl);

        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Collections.singletonList(
                        new HashMap<String, Object>() {
                            {
                                put("audio", fileUrl);
                            }
                        }))
                .build();

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(dashScopeConfig.getApiKey())
                .model(dashScopeConfig.getSummarizationModel())
                .message(userMessage)
                .build();

        try {
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalConversationResult result = conv.call(param);
            String summary = result.getOutput()
                    .getChoices().get(0)
                    .getMessage().getContent().get(0)
                    .get("text").toString();
            log.debug("英文摘要生成成功");
            return summary;
        } catch (Exception e) {
            log.error("英文摘要生成失败", e);
            throw new RuntimeException("英文摘要生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 翻译摘要为中文
     *
     * @param englishSummary 英文摘要
     * @return 中文摘要
     */
    public String translateSummary(String englishSummary) {
        log.debug("开始翻译摘要");

        String prompt = "请将以下英文内容进行不多于 100 字的中文摘要分析。直接输出结果, 结果不用携带字数说明：\n" + englishSummary;

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(dashScopeConfig.getApiKey())
                .model(dashScopeConfig.getTranslationModel())
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        try {
            Generation gen = new Generation();
            GenerationResult result = gen.call(param);
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();
            log.debug("翻译完成");
            return response;
        } catch (Exception e) {
            log.error("翻译失败", e);
            throw new RuntimeException("翻译失败: " + e.getMessage(), e);
        }
    }
}
