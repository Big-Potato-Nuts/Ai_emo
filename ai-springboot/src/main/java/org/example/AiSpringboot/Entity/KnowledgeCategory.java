package org.example.AiSpringboot.Entity;

// 知识文章分类实体类，对应数据库表 knowledge_category

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
 * 知识文章分类实体
 * 对应表：knowledge_category（知识库分类，支持父子层级树）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_category")
public class KnowledgeCategory {

    // 分类ID（主键，自增）
    @TableId(type = IdType.AUTO)
    private Long id;

    // 父分类ID（0 表示顶级分类）
    @TableField("parent_id")
    private Long parentId;

    // 分类名称（如：情绪管理、睡眠健康、压力应对）
    @TableField("category_name")
    private String categoryName;

    // 分类代码（唯一，用于程序内引用）
    @TableField("category_code")
    private String categoryCode;

    // 分类描述
    private String description;

    // 排序号（数字越小越靠前）
    @TableField("sort_order")
    private Integer sortOrder;

    // 状态：0-禁用 1-启用
    private Integer status;

    // 创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 更新时间
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
