package org.example.AiSpringboot.Service;

import org.example.AiSpringboot.DTO.Response.FileUploadResponseDTO;
import org.example.AiSpringboot.Entity.SysFileInfo;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Mapper.SysFileInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 * 职责：
 * 1. 校验上传文件（非空、大小限制、类型白名单）
 * 2. 保存文件到服务器 uploads 目录（文件名用 UUID 防止冲突）
 * 3. 在 sys_file_info 表登记文件元信息（业务类型/业务ID/上传人），便于追踪
 * 4. 返回文件访问相对路径（前端拼接域名访问）
 */
@Service
public class FileService {

    // 允许上传的文件类型白名单（IMAGE: 图片；DOC: 文档；其他按需扩展）
    private static final List<String> ALLOWED_TYPES = Arrays.asList("IMG", "DOC", "PDF", "TXT");

    @Autowired
    private SysFileInfoMapper sysFileInfoMapper;

    /**
     * 上传文件
     *
     * @param file          上传的文件（MultipartFile）
     * @param businessType  业务类型（如 ARTICLE 表示文章封面）
     * @param businessId    业务对象ID（如文章ID）
     * @param businessField 业务字段名（如 cover 表示封面字段）
     * @param uploadUserId  上传人用户ID
     * @return 上传结果（含 filePath，前端拼接 fileBaseUrl 后即可访问图片）
     */
    public FileUploadResponseDTO upload(MultipartFile file, String businessType,
                                        String businessId, String businessField, Long uploadUserId) {
        // 1. 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        // 2. 校验文件大小（上限 5MB，与前端 ArticleDialog 校验一致）
        long maxSize = 5L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过5MB");
        }
        // 3. 校验文件类型（按 MIME 前缀判断，仅允许图片和常见文档）
        String contentType = file.getContentType();
        String fileType = classifyFileType(contentType);
        if (!ALLOWED_TYPES.contains(fileType)) {
            throw new BusinessException("不支持的文件类型，请上传图片或文档");
        }

        // 4. 生成存储目录：程序运行目录下的 uploads（jar 包同级的 uploads 文件夹）
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        // 5. 目录不存在则创建（多级目录一起创建）
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 6. 生成唯一文件名：UUID + 原始文件扩展名（保留扩展名便于浏览器识别类型）
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storeName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 7. 保存文件到磁盘
        File dest = new File(dir, storeName);
        try {
            // transferTo 为 Spring 提供的原子写盘方法
            file.transferTo(dest);
        } catch (IOException e) {
            // 写盘失败抛出业务异常，避免前端误以为上传成功
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }

        // 8. 登记文件元信息到 sys_file_info 表
        SysFileInfo fileInfo = SysFileInfo.builder()
                .originalName(originalName != null ? originalName : storeName)
                .filePath("/uploads/" + storeName) // 访问相对路径
                .fileSize(file.getSize())
                .fileType(fileType)
                .businessType(businessType)
                .businessId(businessId)
                .businessField(businessField)
                .uploadUserId(uploadUserId)
                .isTemp(0) // 非临时文件
                .status(1) // 正常状态
                .createTime(LocalDateTime.now())
                .build();
        sysFileInfoMapper.insert(fileInfo);

        // 9. 组装响应 DTO 返回
        return FileUploadResponseDTO.builder()
                .id(fileInfo.getId())
                .originalName(fileInfo.getOriginalName())
                .filePath(fileInfo.getFilePath())
                .fileSize(fileInfo.getFileSize())
                .fileType(fileInfo.getFileType())
                .createTime(fileInfo.getCreateTime())
                .build();
    }

    /**
     * 根据 MIME 类型归类文件类型
     * 说明：image/* 归为图片；pdf/word/excel/txt 归为文档；其他返回 UNKNOWN
     */
    private String classifyFileType(String contentType) {
        if (contentType == null) {
            return "UNKNOWN";
        }
        // 图片类型
        if (contentType.startsWith("image/")) {
            return "IMG";
        }
        // 常见文档类型
        if (contentType.equals("application/pdf")) {
            return "PDF";
        }
        if (contentType.contains("msword") || contentType.contains("wordprocessingml")) {
            return "DOC";
        }
        if (contentType.contains("spreadsheetml") || contentType.contains("excel")) {
            return "XLS";
        }
        if (contentType.startsWith("text/")) {
            return "TXT";
        }
        return "UNKNOWN";
    }
}
