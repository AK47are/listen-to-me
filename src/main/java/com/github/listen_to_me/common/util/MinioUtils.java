package com.github.listen_to_me.common.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.github.listen_to_me.common.enumeration.RedisKey;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlPath;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MinioUtils {

    private static MinioClient CLIENT;
    private static String BUCKET;
    private static Integer EXPIRATION;
    private static Integer EXPIRATION_RAW;
    private static Integer EXPIRATION_CLIP;

    @Autowired
    public void setCLIENT(MinioClient minioClient) {
        MinioUtils.CLIENT = minioClient;
    }

    @Value("${minio.bucket-name}")
    public void setBUCKET(String bucket) {
        MinioUtils.BUCKET = bucket;
    }

    @Value("${minio.expiration}")
    public void setEXPIRATION(Integer expiration) {
        MinioUtils.EXPIRATION = expiration;
    }

    @Value("${minio.raw-expiration}")
    public void setEXPIRATION_RAW(Integer expirationRaw) {
        MinioUtils.EXPIRATION_RAW = expirationRaw;
    }

    @Value("${minio.clip-expiration}")
    public void setEXPIRATION_CLIP(Integer expirationClip) {
        MinioUtils.EXPIRATION_CLIP = expirationClip;
    }

    /**
     * 生成规范化的存储路径
     * 模块名/日期/子模块/文件名
     */
    private static String getPath(String module, String objectName) {
        return StrUtil.removePrefix(UrlPath.of(module, null)
                .add(DateTime.now().toString("yyyyMMdd"))
                .add(objectName).build(null), "/");
    }

    /**
     * 生成临时访问链接
     */
    public static String generatePresignedUrl(RedisKey redisKey, String fullPath,
            Integer expiration, TimeUnit unit) {
        String presignedUrl = RedisUtils.get(redisKey, fullPath);
        if (StrUtil.isNotBlank(presignedUrl)) {
            return presignedUrl;
        }
        try {
            presignedUrl = CLIENT.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(BUCKET)
                            .object(fullPath)
                            .expiry(expiration, unit)
                            .build());
            RedisUtils.set(redisKey, fullPath, presignedUrl);
            return presignedUrl;
        } catch (Exception e) {
            log.warn("生成临时链接失败", e);
        }
        return null;
    }

    /**
     * 获取临时访问链接
     */
    public static String getPresignedUrl(String fullPath) {
        if (StrUtil.isBlank(fullPath)) {
            return null;
        }
        if (StrUtil.contains(fullPath, "/audio/clip/")) {
            log.info("生成临时音频资源临时链接 - 完整路径: {}", fullPath);
            return generatePresignedUrl(RedisKey.ONLINE_CLIP_URL, fullPath,
                    EXPIRATION_CLIP, TimeUnit.MINUTES);
        } else if (StrUtil.contains(fullPath, "/audio/")) {
            log.info("生成完整音频资源临时链接 - 完整路径: {}", fullPath);
            return generatePresignedUrl(RedisKey.ONLINE_RAW_URL, fullPath,
                    EXPIRATION_RAW, TimeUnit.HOURS);
        } else {
            log.info("生成图片资源临时链接 - 完整路径: {}", fullPath);
            return generatePresignedUrl(RedisKey.ONLINE_IMG_URL, fullPath,
                    EXPIRATION, TimeUnit.HOURS);
        }
    }

    /**
     * 流式上传文件
     */
    public static String uploadFile(MultipartFile file, String module, String objectName)
            throws Exception {
        objectName = IdUtil.randomUUID() + "_" + objectName;
        log.info("上传文件 - 模块: {}, 文件名: {}", module, objectName);
        String fullPath = getPath(module, objectName);
        CLIENT.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(fullPath)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        return fullPath;
    }

    /**
     * 复制原文件到目标模块
     */
    public static String copyFile(String sourceFile, String targetModule) throws Exception {
        log.info("复制文件 - 原文件: {}, 目标模块: {}", sourceFile, targetModule);
        String fileName = FileUtil.getName(sourceFile);
        String targetPath = getPath(targetModule, fileName);

        CLIENT.copyObject(CopyObjectArgs.builder()
                .bucket(BUCKET)
                .object(targetPath)
                .source(CopySource.builder().bucket(BUCKET).object(sourceFile).build())
                .build());
        return targetPath;
    }

    /**
     * 删除文件
     */
    public static void removeFile(String fullPath) throws Exception {
        log.info("删除文件 - 完整路径: {}", fullPath);
        CLIENT.removeObject(RemoveObjectArgs.builder()
                .bucket(BUCKET)
                .object(fullPath)
                .build());
    }

    /**
     * 下载文件到本地
     */
    public static String downloadToLocal(String objectName) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "minio_temp_" + IdUtil.randomUUID() + "_" + System.currentTimeMillis();
        File localFile = new File(tempDir, fileName);

        try (InputStream in = CLIENT.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectName)
                        .build())) {
            Files.copy(in, localFile.toPath());
            log.info("MinIO 下载文件到本地成功：{} → {}", objectName, localFile.getAbsolutePath());
            return localFile.getAbsolutePath();
        } catch (Exception e) {
            log.error("MinIO 下载文件失败：{}", objectName, e);
            throw new RuntimeException("下载文件失败：" + objectName, e);
        }
    }

    /**
     * 上传本地文件到 MinIO
     */
    public static String uploadLocalFile(String localFilePath, String module) {
        File file = new File(localFilePath);
        String fileName = file.getName();

        try {
            String fullPath = getPath(module, fileName);
            log.info("MinIO 上传本地文件 - 文件路径：{} → 模块：{}，路径：{}", localFilePath, module, fullPath);

            CLIENT.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fullPath)
                            .filename(localFilePath)
                            .contentType(Files.probeContentType(file.toPath()))
                            .build());
            return fullPath;
        } catch (Exception e) {
            log.error("MinIO 上传本地文件失败 - 文件路径：{}", localFilePath, e);
            throw new RuntimeException("MinIO 上传本地文件失败 - 文件路径：" + localFilePath, e);
        }
    }

}
