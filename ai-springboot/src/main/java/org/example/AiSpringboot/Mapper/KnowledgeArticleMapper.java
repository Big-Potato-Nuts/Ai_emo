package org.example.AiSpringboot.Mapper;

// 知识文章 Mapper 接口，对应表 knowledge_article

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.AiSpringboot.DTO.Response.KnowledgeArticleResponseDTO;
import org.example.AiSpringboot.Entity.KnowledgeArticle;

import java.util.List;

/**
 * 知识文章 Mapper
 * 除了继承 BaseMapper 的基础能力外，还提供联表分页查询（带分类名称、作者名称）
 */
@Mapper
public interface KnowledgeArticleMapper extends BaseMapper<KnowledgeArticle> {

    /**
     * 分页查询文章列表（联表查出分类名称和作者名称）
     * 说明：
     * 1. 使用 LEFT JOIN 关联分类表和用户表，把 categoryName / authorName 一起查出来
     * 2. 支持按标题模糊搜索、按分类筛选、按状态筛选
     * 3. onlyPublished = true 时只查已发布文章（前台用户端使用）
     *
     * @param page           MyBatis-Plus 分页对象（自动生成 LIMIT 并回填 total）
     * @param title          文章标题关键词（模糊匹配，可为空）
     * @param categoryId     分类ID（可为空）
     * @param status         文章状态（可为空）
     * @param onlyPublished  是否只查已发布文章（true=前台用户端，false=后台管理端）
     * @param orderField     排序字段（白名单校验后传入，防止 SQL 注入）
     * @param isDesc         是否倒序（true=倒序，false=正序）
     * @return 分页结果，records 中每条包含文章字段 + categoryName + authorName
     */
    @Select("<script>" +
            "SELECT a.id, a.category_id, a.title, a.summary, a.content, a.cover_image, a.tags, " +
            "       a.author_id, a.read_count, a.status, a.published_at, a.created_at, a.updated_at, " +
            "       c.category_name AS categoryName, " +
            "       COALESCE(u.nickname, u.username) AS authorName " +
            "FROM knowledge_article a " +
            "LEFT JOIN knowledge_category c ON a.category_id = c.id " +
            "LEFT JOIN user u ON a.author_id = u.id " +
            "<where>" +
            "  <if test='title != null and title != \"\"'> AND a.title LIKE CONCAT('%', #{title}, '%')</if>" +
            "  <if test='categoryId != null'> AND a.category_id = #{categoryId}</if>" +
            "  <if test='status != null'> AND a.status = #{status}</if>" +
            "  <if test='onlyPublished'> AND a.status = 1</if>" +
            "</where>" +
            "ORDER BY ${orderField} ${orderDirection}" +
            "</script>")
    IPage<KnowledgeArticleResponseDTO> selectArticlePage(Page<KnowledgeArticleResponseDTO> page,
                                                         @Param("title") String title,
                                                         @Param("categoryId") Long categoryId,
                                                         @Param("status") Integer status,
                                                         @Param("onlyPublished") boolean onlyPublished,
                                                         @Param("orderField") String orderField,
                                                         @Param("orderDirection") String orderDirection);

    /**
     * 根据文章ID查询详情（联表带出分类名称、作者名称、标签数组）
     * 说明：标签以逗号分隔存储在 tags 字段中，这里原样返回，由响应 DTO 负责拆分为数组
     *
     * @param articleId 文章ID（UUID 字符串）
     * @return 文章详情 DTO
     */
    @Select("SELECT a.id, a.category_id, a.title, a.summary, a.content, a.cover_image, a.tags, " +
            "       a.author_id, a.read_count, a.status, a.published_at, a.created_at, a.updated_at, " +
            "       c.category_name AS categoryName, " +
            "       COALESCE(u.nickname, u.username) AS authorName " +
            "FROM knowledge_article a " +
            "LEFT JOIN knowledge_category c ON a.category_id = c.id " +
            "LEFT JOIN user u ON a.author_id = u.id " +
            "WHERE a.id = #{articleId}")
    KnowledgeArticleResponseDTO selectArticleDetail(@Param("articleId") String articleId);
}
