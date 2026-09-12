package org.example.AiSpringboot.Entity;

// 系统文件信息实体类，对应数据库表 sys_file_info

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统文件信息实体
 * 对应表：sys_file_info（记录用户上传的每一个文件，用于追踪和统一管理）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_file_info")
public class SysFileInfo {

    // 文件ID（主键，自增）
    @TableId(type = IdType.AUTO)
    private Long id;

    // 原始文件名（用户上传时的文件名，如：封面.png）
    @TableField("original_name")
    private String originalName;

    // 文件访问路径（服务器存储的相对路径，如：/uploads/xxxx.png）
    @TableField("file_path")
    private String filePath;

    // 文件大小（单位：字节）
    @TableField("file_size")
    private Long fileSize;

    // 文件类型（IMG/PDF/TXT/DOC/XLS 等）
    @TableField("file_type")
    private String fileType;

    // 业务类型（用于区分文件用途，如：ARTICLE 表示文章封面）
    @TableField("business_type")
    private String businessType;

    // 业务对象ID（关联的业务数据主键，如文章ID）
    @TableField("business_id")
    private String businessId;

    // 业务字段名（对应业务表中的字段名，如：cover）
    @TableField("business_field")
    private String businessField;

    // 上传用户ID（记录是谁上传的文件）
    @TableField("upload_user_id")
    private Long uploadUserId;

    // 是否临时文件（0-否 1-是，临时文件会过期清理）
    @TableField("is_temp")
    private Integer isTemp;

    // 状态（0-删除 1-正常）
    private Integer status;

    // 创建时间
    @TableField("create_time")
    private LocalDateTime createTime;

    // 过期时间（仅对临时文件有效）
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
