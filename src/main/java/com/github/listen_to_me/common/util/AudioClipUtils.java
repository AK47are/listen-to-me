package com.github.listen_to_me.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AudioClipUtils {

    /**
     * 截取音频前 N 秒
     * 
     * @param sourcePath 原音频路径（MinIO 下载后的本地路径）
     * @param outputPath 输出片段路径
     * @param seconds    截取几秒（如 10 → 前10秒）
     * @return 是否成功
     */
    public static boolean clipAudioStart(String sourcePath, String outputPath, int seconds) {
        try {
            String command = StrUtil.format(
                    "ffmpeg -i \"{}\" -ss 0 -t {} -y \"{}\"",
                    sourcePath,
                    seconds,
                    outputPath);

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("音频截取输出 - line:{}", line);
            }
            process.waitFor();
            return true;
        } catch (Exception e) {
            log.error("音频截取错误 - 错误信息:{}", e.getMessage());
            return false;
        }
    }
}
