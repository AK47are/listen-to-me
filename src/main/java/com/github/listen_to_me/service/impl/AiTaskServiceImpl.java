package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.producer.AiTaskProducer;
import com.github.listen_to_me.domain.entity.AiTask;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.domain.vo.AiTaskVO;
import com.github.listen_to_me.mapper.AiTaskMapper;
import com.github.listen_to_me.service.IAiTaskService;
import com.github.listen_to_me.service.IAudioInfoService;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl extends ServiceImpl<AiTaskMapper, AiTask> implements IAiTaskService {

    private final AiTaskProducer aiTaskProducer;
    private final IAudioInfoService audioInfoService;

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
}
