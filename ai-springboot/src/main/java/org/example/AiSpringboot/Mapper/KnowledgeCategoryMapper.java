package org.example.AiSpringboot.Mapper;

// 知识文章分类 Mapper 接口，对应表 knowledge_category

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.AiSpringboot.Entity.KnowledgeCategory;

/**
 * 知识文章分类 Mapper
 * 继承 BaseMapper 后自动获得分类表的增删改查能力
 */
@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {
}
