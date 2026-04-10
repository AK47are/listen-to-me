package com.github.listen_to_me.common.consumer;

import java.io.File;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.github.listen_to_me.common.config.AudioTranscodeMqConfig;
import com.github.listen_to_me.common.util.AudioClipUtils;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.dto.TranscodeTaskDTO;
import com.github.listen_to_me.domain.entity.AudioInfo;
import com.github.listen_to_me.mapper.AudioInfoMapper;
import com.github.listen_to_me.service.IAudioInfoService;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 音频转码消费者
 */
@Slf4j
@Component
@AllArgsConstructor
public class AudioTranscodeConsumer {

    private IAudioInfoService audioInfoService;
    private AudioInfoMapper audioInfoMapper;

    /**
     * 监听音频转码队列
     */
    @RabbitListener(queues = AudioTranscodeMqConfig.QUEUE_NAME)
    public void handleTranscodeTask(String jsonMsg) {
        TranscodeTaskDTO taskDTO = JSONUtil.toBean(jsonMsg, TranscodeTaskDTO.class);

        Long audioId = taskDTO.getAudioId();
        String objectName = taskDTO.getObjectName();
        Integer trialDuration = taskDTO.getTrialDuration();

        log.debug("收到转码任务 - audioId:{}，文件路径：{}", audioId, objectName);

        try {
            AudioInfo audioInfo = audioInfoMapper.selectById(audioId);
            if (audioInfo.getClipPath() != null) {
                MinioUtils.removeFile(audioInfo.getClipPath());
            }
            // 1. 更新数据库状态为 转码中 (TRANSCODING)
            audioInfoMapper.updateStatusById(audioId, "TRANSCODING");
            // 1. 从 MinIO 下载原音频到本地临时文件
            String sourcePath = MinioUtils.downloadToLocal(objectName);

            // 2. 输出片段路径
            File clipFile = File.createTempFile("clip_" + audioId + "_", ".mp3");
            String clipPath = clipFile.getAbsolutePath();

            // ================== 截取前 trialDuration 秒 ==================
            AudioClipUtils.clipAudioStart(sourcePath, clipPath, trialDuration);

            // 3. 上传片段到 MinIO
            String clipMinioPath = MinioUtils.uploadLocalFile(clipPath, "/online/audio/clip");

            log.debug("音频片段生成成功 - audioId:{}", audioId);
            // 3. 更新数据库状态为 已上线 (ONLINE)，并写入新的播放地址
            audioInfoMapper.updateStatusAndClipPathById(audioId, "ONLINE", clipMinioPath);
            audioInfoService.MoveAudioToOnline(audioId);
            log.debug("转码任务完成 - audioId:{}", audioId);

        } catch (Exception e) {
            log.error("转码任务失败 - audioId:{}, 错误信息：{}", audioId, e.getMessage());
            audioInfoMapper.updateStatusById(audioId, "FAILED");
        }
    }
}
