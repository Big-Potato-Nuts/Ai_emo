package org.example.AiSpringboot.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.AiSpringboot.DTO.Response.KnowledgeCategoryResponseDTO;
import org.example.AiSpringboot.Entity.KnowledgeCategory;
import org.example.AiSpringboot.Mapper.KnowledgeCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识分类服务
 * 职责：查询启用状态的分类并组装成树形结构（支持父子分类层级）
 */
@Service
public class KnowledgeCategoryService {

    @Autowired
    private KnowledgeCategoryMapper knowledgeCategoryMapper;

    /**
     * 查询分类树
     * 说明：只返回启用的分类（status=1），按排序号升序；
     *      顶级分类（parent_id=0）作为树的根节点，子分类挂到对应父节点的 children 下
     *
     * @return 分类树列表（每项含 children 子列表）
     */
    public List<KnowledgeCategoryResponseDTO> categoryTree() {
        // 1. 查询所有启用状态的分类，按排序号升序
        LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeCategory::getStatus, 1)
                .orderByAsc(KnowledgeCategory::getSortOrder)
                .orderByAsc(KnowledgeCategory::getId);
        List<KnowledgeCategory> categories = knowledgeCategoryMapper.selectList(wrapper);

        // 2. 实体转响应 DTO（保留完整字段）
        List<KnowledgeCategoryResponseDTO> dtoList = categories.stream().map(c -> KnowledgeCategoryResponseDTO.builder()
                .id(c.getId())
                .parentId(c.getParentId())
                .categoryName(c.getCategoryName())
                .categoryCode(c.getCategoryCode())
                .description(c.getDescription())
                .sortOrder(c.getSortOrder())
                .status(c.getStatus())
                .children(new ArrayList<>())
                .build()).toList();

        // 3. 按 parentId 分组，方便快速挂载子节点
        Map<Long, List<KnowledgeCategoryResponseDTO>> byParent = dtoList.stream()
                .collect(Collectors.groupingBy(KnowledgeCategoryResponseDTO::getParentId));

        // 4. 组装树：把每个节点的子节点挂到 children 里
        List<KnowledgeCategoryResponseDTO> tree = new ArrayList<>();
        for (KnowledgeCategoryResponseDTO node : dtoList) {
            // 给当前节点挂上它的直接子节点
            node.setChildren(byParent.getOrDefault(node.getId(), new ArrayList<>()));
            // parentId 为 0 的是顶级节点，作为树的根
            if (node.getParentId() != null && node.getParentId() == 0L) {
                tree.add(node);
            }
        }
        return tree;
    }
}
