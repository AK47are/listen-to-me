package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.entity.AiTask;
import com.github.listen_to_me.domain.vo.AiTaskVO;

public interface IAiTaskService extends IService<AiTask> {

    /**
     * 根据 taskId 查询任务详情
     *
     * @param userId 当前用户ID
     * @param taskId 任务ID
     * @return 任务VO
     */
    AiTaskVO getTaskById(Long userId, String taskId);

    /**
     * 创建音频转写任务
     * 
     * @param userId  用户ID
     * @param audioId 音频ID
     * @return 任务信息
     */
    AiTaskVO createTranscriptionTask(Long userId, Long audioId);
}
