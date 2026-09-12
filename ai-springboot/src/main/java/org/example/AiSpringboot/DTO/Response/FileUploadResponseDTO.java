package org.example.AiSpringboot.DTO.Response;

// 文件上传结果响应 DTO

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件上传结果
 * 前端 ArticleDialog.vue 使用 fileRes.filePath 拼接完整访问地址（fileBaseUrl + filePath）
 */
@Data
@Builder
public class FileUploadResponseDTO {

    // 文件ID
    private Long id;

    // 原始文件名（用户上传时的名称）
    private String originalName;

    // 文件访问相对路径（如：/uploads/xxxx.png，前端拼接域名使用）
    private String filePath;

    // 文件大小（字节）
    private Long fileSize;

    // 文件类型（IMG/PDF/TXT/DOC/XLS）
    private String fileType;

    // 上传时间
    private LocalDateTime createTime;
}
