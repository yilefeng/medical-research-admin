package com.medical.research.util;

import com.medical.research.config.MinioConfig;
import com.medical.research.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * MinIO工具类：文件上传、删除、桶初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {
    private final MinioConfig minioConfig;
    private final io.minio.MinioClient minioClient;

    /**
     * 初始化存储桶（不存在则创建）
     */
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
                log.info("MinIO存储桶创建成功: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("MinIO初始化存储桶失败", e);
            throw new BusinessException("MinIO存储桶初始化失败");
        }
    }

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) {
        try {
            initBucket();
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new BusinessException("文件名不能为空");
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;

            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 返回文件访问路径
            String fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + fileName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("MinIO文件上传失败", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传字节数组（如PDF）
     */
    public String uploadBytes(byte[] bytes, String fileName) {
        try {
            initBucket();
            InputStream inputStream = new ByteArrayInputStream(bytes);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, bytes.length, -1)
                    .contentType("application/pdf")
                    .build());

            String fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + fileName;
            log.info("字节数组上传成功: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("MinIO字节数组上传失败", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .build());
            log.info("文件删除成功: {}", fileName);
        } catch (Exception e) {
            log.error("MinIO文件删除失败", e);
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }
}