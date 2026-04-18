package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.producer.AiTaskProducer;
import com.github.listen_to_me.domain.entity.AiTask;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.entity.AudioTranscript;
import com.github.listen_to_me.domain.vo.AiTaskVO;
import com.github.listen_to_me.mapper.AiTaskMapper;
import com.github.listen_to_me.mapper.AudioTranscriptMapper;
import com.github.listen_to_me.service.IAiTaskService;
import com.github.listen_to_me.service.IAudioInfoService;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl extends ServiceImpl<AiTaskMapper, AiTask> implements IAiTaskService {

    private final AiTaskProducer aiTaskProducer;
    private final IAudioInfoService audioInfoService;
    private final AudioTranscriptMapper audioTranscriptMapper;

    @Override
    public AiTaskVO getTaskById(Long userId, String taskId) {
        AiTask task = getOne(Wrappers.<AiTask>lambdaQuery().eq(AiTask::getTaskId, taskId));
        if (task == null) {
            throw new BaseException(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            throw new BaseException(403, "无权访问该任务");
        }

        AiTaskVO vo = new AiTaskVO();
        vo.setTaskId(task.getTaskId());
        vo.setType(task.getType());
        vo.setStatus(task.getStatus());

        if (task.getResult() != null) {
            vo.setResult(JSONUtil.parse(task.getResult()));
        }

        vo.setFailReason(task.getFailReason());
        return vo;
    }

    @Override
    public AiTaskVO createTranscriptionTask(Long userId, Long audioId) {
        log.debug("创建音频转写任务 - 用户ID: {}, 音频ID: {}", userId, audioId);

        // 校验音频是否存在且属于当前用户
        AudioInfo audio = audioInfoService.getById(audioId);
        if (audio == null || !audio.getCreatorId().equals(userId)) {
            throw new BaseException(404, "音频不存在");
        }

        // 生成任务ID
        String taskId = IdUtil.fastSimpleUUID();

        // 创建任务记录
        AiTask task = new AiTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setAudioId(audioId);
        task.setType("TRANSCRIPTION");
        task.setStatus("PENDING");

        aiTaskProducer.sendTranscriptionTask(taskId, audioId);

        save(task);
        return getTaskById(userId, taskId);
    }

    @Override
    @Transactional
    public void confirmTranscript(Long userId, String taskId) {
        log.debug("确认转写结果 - 用户ID: {}, 任务ID: {}", userId, taskId);

        AiTask task = getOne(Wrappers.<AiTask>lambdaQuery().eq(AiTask::getTaskId, taskId));

        if (task == null) {
            throw new BaseException(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            throw new BaseException(403, "无权操作该任务");
        }

        if (!"SUCCESS".equals(task.getStatus())) {
            throw new BaseException(400, "任务未完成，无法确认");
        }

        JSONObject resultJson = JSONUtil.parseObj(task.getResult());
        JSONArray transcripts = resultJson.getJSONArray("transcripts");
        if (transcripts == null || transcripts.isEmpty()) {
            throw new BaseException(400, "转写结果为空");
        }

        JSONObject firstTranscript = transcripts.getJSONObject(0);
        String fullText = firstTranscript.getStr("text");
        JSONArray words = firstTranscript.getJSONArray("sentences").getJSONObject(0).getJSONArray("words");

        audioTranscriptMapper
                .delete(Wrappers.<AudioTranscript>lambdaQuery().eq(AudioTranscript::getAudioId, task.getAudioId()));

        AudioTranscript transcript = new AudioTranscript();
        transcript.setAudioId(task.getAudioId());
        transcript.setTaskId(taskId);
        transcript.setFullText(fullText);
        transcript.setSegmentJson(words.toString());
        audioTranscriptMapper.insert(transcript);

        log.debug("确认转写结果成功 - taskId: {}, audioId: {}", taskId, task.getAudioId());
    }

    @Override
    public AiTaskVO createSummarizationTask(Long userId, Long audioId) {
        log.debug("创建音频摘要任务 - 用户ID: {}, 音频ID: {}", userId, audioId);

        AudioInfo audio = audioInfoService.getById(audioId);
        if (audio == null || !audio.getCreatorId().equals(userId)) {
            throw new BaseException(404, "音频不存在");
        }

        String taskId = IdUtil.fastSimpleUUID();

        AiTask task = new AiTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setAudioId(audioId);
        task.setType("SUMMARIZATION");
        task.setStatus("PENDING");

        aiTaskProducer.sendSummarizationTask(taskId, audioId);
        save(task);

        return getTaskById(userId, taskId);
    }
}
