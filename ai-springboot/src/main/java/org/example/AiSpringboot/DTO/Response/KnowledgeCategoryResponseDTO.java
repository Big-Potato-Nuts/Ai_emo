package org.example.AiSpringboot.DTO.Response;

// 知识分类树节点响应 DTO

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识分类树节点
 * 支持父子层级：顶级分类的 children 里挂子分类
 */
@Data
@Builder
public class KnowledgeCategoryResponseDTO {

    // 分类ID
    private Long id;

    // 父分类ID（0=顶级）
    private Long parentId;

    // 分类名称
    private String categoryName;

    // 分类代码
    private String categoryCode;

    // 分类描述
    private String description;

    // 排序号
    private Integer sortOrder;

    // 状态（0-禁用 1-启用）
    private Integer status;

    // 子分类列表（树形结构，默认空列表）
    private List<KnowledgeCategoryResponseDTO> children;

    /**
     * 便捷方法：安全获取子分类列表（避免 null 判断）
     */
    public List<KnowledgeCategoryResponseDTO> getChildren() {
        return children == null ? new ArrayList<>() : children;
    }
}
