package com.github.listen_to_me.common.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlPath;
import cn.hutool.core.util.StrUtil;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MinioUtils {

    private static MinioClient CLIENT;
    private static String BUCKET;
    private static Integer EXPIRATION;

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

    /**
     * 生成规范化的存储路径
     * 模块名/日期/子模块/文件名
     */
    public static String getPath(String module, String objectName) {
        return StrUtil.removePrefix(UrlPath.of(module, null)
                .add(DateTime.now().toString("yyyyMMdd"))
                .add(objectName).build(null), "/");
    }

    /**
     * 生成临时访问链接
     */
    public static String getPresignedUrl(String module, String objectName) throws Exception {
        String url = CLIENT.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(BUCKET)
                        .object(getPath(module, objectName))
                        .expiry(EXPIRATION, TimeUnit.MINUTES)
                        .build());
        log.info("生成临时链接 - 模块: {}, 文件名: {}, URL: ", module, objectName, url);
        return url;
    }

    public static String getPresignedUrl(String fullPath) throws Exception {
        log.info("生成临时链接 - 完整路径: ", fullPath);
        return CLIENT.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(BUCKET)
                        .object(fullPath)
                        .expiry(EXPIRATION, TimeUnit.MINUTES)
                        .build());
    }

    /**
     * 流式上传文件
     */
    public static void uploadFile(MultipartFile file, String module, String objectName)
            throws Exception {
        log.info("上传文件 - 模块: {}, 文件名: {}", module, objectName);
        CLIENT.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(getPath(module, objectName))
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
    }

    /**
     * 复制原文件到目标模块
     */
    public static void copyFile(String sourceFile, String targetModule) throws Exception {
        log.info("复制文件 - 原文件: {}, 目标模块: {}", sourceFile, targetModule);
        String fileName = FileUtil.getName(sourceFile);
        String targetPath = getPath(targetModule, fileName);

        CLIENT.copyObject(CopyObjectArgs.builder()
                .bucket(BUCKET)
                .object(targetPath)
                .source(CopySource.builder().bucket(BUCKET).object(sourceFile).build())
                .build());
    }

    /**
     * 删除文件
     */
    public static void removeFile(String fullPath) throws Exception {
        log.info("删除文件 - 完整路径: ", fullPath);
        CLIENT.removeObject(RemoveObjectArgs.builder()
                .bucket(BUCKET)
                .object(fullPath)
                .build());
    }
}
