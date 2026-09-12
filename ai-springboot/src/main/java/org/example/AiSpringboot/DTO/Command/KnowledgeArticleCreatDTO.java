package org.example.AiSpringboot.DTO.Command;

// 新增/更新知识文章请求 DTO

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识文章创建/更新请求体
 * 字段与 ArticleDialog.vue 提交的数据一致：
 * id（新增时前端生成的 UUID）、title、categoryId、summary、content、coverImage、tags
 */
@Data
public class KnowledgeArticleCreatDTO {

    // 文章ID（新增时由前端生成 UUID 传入；编辑时使用路径中的 ID）
    @Size(max = 36, message = "文章ID长度不能超过36个字符")
    private String id;

    // 文章标题（必填，最多200字）
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    // 所属分类ID（必填）
    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    // 文章摘要（可选）
    @Size(max = 1000, message = "文章摘要不能超过1000个字符")
    private String summary;

    // 文章正文（富文本 HTML，必填）
    @NotBlank(message = "文章内容不能为空")
    private String content;

    // 封面图片相对路径（可选）
    @Size(max = 500, message = "封面图片路径不能超过500个字符")
    private String coverImage;

    // 标签（逗号分隔字符串，可选）
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;
}
