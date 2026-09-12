package org.example.AiSpringboot.DTO.Response;

// 知识文章响应 DTO（列表与详情共用）

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 知识文章响应体
 * 在文章实体字段基础上，补充联表查询出的分类名称、作者名称，
 * 并把逗号分隔的 tags 拆成 tagArray 数组（前端标签展示需要）
 */
@Data
public class KnowledgeArticleResponseDTO {

    // 文章ID（UUID 字符串）
    private String id;

    // 分类ID
    private Long categoryId;

    // 分类名称（联表查出，前端列表标签展示）
    private String categoryName;

    // 文章标题
    private String title;

    // 文章摘要
    private String summary;

    // 文章正文（富文本 HTML）
    private String content;

    // 封面图片相对路径
    private String coverImage;

    // 标签（逗号分隔的原始字符串）
    private String tags;

    // 作者ID
    private Long authorId;

    // 作者名称（联表查出，取昵称，昵称为空回退用户名）
    private String authorName;

    // 阅读次数
    private Integer readCount;

    // 状态：0-草稿 1-已发布 2-已下线
    private Integer status;

    // 发布时间
    private LocalDateTime publishedAt;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;

    /**
     * 获取标签数组
     * 说明：数据库中 tags 用英文逗号分隔存储，这里按逗号拆分返回数组；
     *      为空时返回空数组，避免前端报错
     */
    public List<String> getTagArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return List.of();
        }
        // 按逗号拆分并去除首尾空格，过滤空串
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
