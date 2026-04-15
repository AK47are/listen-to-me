package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.domain.entity.AiTask;
import com.github.listen_to_me.domain.vo.AiTaskVO;
import com.github.listen_to_me.mapper.AiTaskMapper;
import com.github.listen_to_me.service.IAiTaskService;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl extends ServiceImpl<AiTaskMapper, AiTask> implements IAiTaskService {

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

}
