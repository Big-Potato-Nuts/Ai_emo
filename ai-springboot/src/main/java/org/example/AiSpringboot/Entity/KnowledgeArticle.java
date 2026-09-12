package org.example.AiSpringboot.Entity;

// 知识文章实体类，对应数据库表 knowledge_article

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
 * 知识文章实体
 * 对应表：knowledge_article（心理健康知识库的文章）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_article")
public class KnowledgeArticle {

    // 文章ID（主键，使用 UUID 字符串，由前端或后端生成）
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    // 分类ID（关联 knowledge_category 表）
    @TableField("category_id")
    private Long categoryId;

    // 文章标题
    private String title;

    // 文章摘要（列表页展示的简介）
    private String summary;

    // 文章正文内容（富文本 HTML）
    private String content;

    // 封面图片（文件相对路径，如 /uploads/xxx.png）
    @TableField("cover_image")
    private String coverImage;

    // 标签（多个标签用英文逗号分隔，如：焦虑,压力,睡眠）
    private String tags;

    // 作者ID（关联 user 表）
    @TableField("author_id")
    private Long authorId;

    // 阅读次数（每次访问详情 +1）
    @TableField("read_count")
    private Integer readCount;

    // 状态：0-草稿 1-已发布 2-已下线
    private Integer status;

    // 发布时间
    @TableField("published_at")
    private LocalDateTime publishedAt;

    // 创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 更新时间（数据库自动更新）
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
