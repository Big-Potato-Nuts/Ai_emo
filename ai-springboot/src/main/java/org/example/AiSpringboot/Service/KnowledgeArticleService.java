package org.example.AiSpringboot.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.AiSpringboot.DTO.Command.KnowledgeArticleCreatDTO;
import org.example.AiSpringboot.DTO.Response.KnowledgeArticleResponseDTO;
import org.example.AiSpringboot.Entity.KnowledgeArticle;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Mapper.KnowledgeArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 知识文章服务
 * 职责：
 * 1. 分页查询文章列表（前台只查已发布、后台查全部，支持标题/分类/状态筛选与排序）
 * 2. 查询文章详情（每次访问阅读数 +1）
 * 3. 新增文章（管理员）
 * 4. 更新文章（管理员）
 * 5. 修改文章上下架状态（管理员）
 * 6. 删除文章（管理员）
 */
@Service
public class KnowledgeArticleService {

    @Autowired
    private KnowledgeArticleMapper knowledgeArticleMapper;

    /**
     * 分页查询文章列表
     *
     * @param pageNum        页码
     * @param pageSize       每页条数
     * @param title          标题模糊搜索词（可为空）
     * @param categoryId     分类ID筛选（可为空）
     * @param status         状态筛选（后台用，可为空）
     * @param isAdmin        是否管理员（管理员=查全部状态，普通用户=只查已发布）
     * @param sortField      排序字段名（前端传 publishedAt/updatedAt/createdAt/readCount）
     * @param sortDirection  排序方向（desc/asc）
     * @return 文章分页结果（含分类名称、作者名称、标签数组）
     */
    public IPage<KnowledgeArticleResponseDTO> pageArticles(long pageNum, long pageSize, String title,
                                                           Long categoryId, Integer status, boolean isAdmin,
                                                           String sortField, String sortDirection) {
        // 1. 构造分页对象（DTO 作为返回类型）
        Page<KnowledgeArticleResponseDTO> page = new Page<>(pageNum, pageSize);

        // 2. 排序字段白名单映射：前端字段名 -> 数据库列名，防止 SQL 注入
        Map<String, String> orderFieldMap = Map.of(
                "publishedAt", "a.published_at",
                "updatedAt", "a.updated_at",
                "createdAt", "a.created_at",
                "readCount", "a.read_count"
        );
        // 3. 未传或非法字段时，默认按发布时间倒序
        String orderField = orderFieldMap.getOrDefault(sortField, "a.published_at");
        // 4. 排序方向只允许 desc/asc，非法时默认 desc
        String orderDirection = "desc".equalsIgnoreCase(sortDirection) ? "desc" : "asc";

        // 5. 普通用户（前台知识库）强制只看已发布文章
        boolean onlyPublished = !isAdmin;

        // 6. 执行联表分页查询（SQL 见 KnowledgeArticleMapper）
        return knowledgeArticleMapper.selectArticlePage(
                page, title, categoryId, status, onlyPublished, orderField, orderDirection);
    }

    /**
     * 查询文章详情（阅读数 +1）
     * 说明：详情页展示需要分类名称和作者名称，走联表查询；
     *      每次访问都让 read_count 自增 1（幂等无影响）
     *
     * @param articleId 文章ID（UUID）
     * @return 文章详情 DTO
     */
    public KnowledgeArticleResponseDTO getArticleDetail(String articleId) {
        // 1. 校验文章是否存在
        if (knowledgeArticleMapper.selectById(articleId) == null) {
            throw new BusinessException("文章不存在");
        }
        // 2. 阅读数 +1（使用 SQL 原子自增，避免并发下先查后改的竞态）
        knowledgeArticleMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<KnowledgeArticle>()
                .eq(KnowledgeArticle::getId, articleId)
                .setSql("read_count = read_count + 1"));
        // 3. 联表查询详情并返回（此时 read_count 已是自增后的最新值）
        return knowledgeArticleMapper.selectArticleDetail(articleId);
    }

    /**
     * 新增文章（管理员）
     * 说明：新增即发布（status=1），发布时间取当前时间，阅读数从 0 开始
     *
     * @param dto      文章内容（标题、分类、摘要、正文、封面、标签）
     * @param authorId 当前登录的管理员用户ID
     * @return 新建后的文章实体
     */
    public KnowledgeArticle createArticle(KnowledgeArticleCreatDTO dto, Long authorId) {
        // 1. 构建文章实体（ID 前端已生成 UUID；若为空则由 MyBatis-Plus 自动生成）
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(dto.getId())
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .content(dto.getContent())
                .coverImage(dto.getCoverImage())
                .tags(dto.getTags())
                .authorId(authorId)
                .readCount(0)
                .status(1) // 新建即发布
                .publishedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        // 2. 写入数据库
        knowledgeArticleMapper.insert(article);
        return article;
    }

    /**
     * 更新文章（管理员）
     * 说明：只更新前端提交的编辑字段，不覆盖阅读数、作者、状态等管理字段
     *
     * @param articleId 文章ID
     * @param dto       编辑后的文章内容
     */
    public void updateArticle(String articleId, KnowledgeArticleCreatDTO dto) {
        // 1. 校验文章是否存在
        if (knowledgeArticleMapper.selectById(articleId) == null) {
            throw new BusinessException("文章不存在");
        }
        // 2. 构建更新字段
        KnowledgeArticle article = new KnowledgeArticle();
        article.setId(articleId);
        article.setCategoryId(dto.getCategoryId());
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setTags(dto.getTags());
        article.setUpdatedAt(LocalDateTime.now());
        // 3. 执行更新（MyBatis-Plus 只更新非 null 字段）
        knowledgeArticleMapper.updateById(article);
    }

    /**
     * 修改文章上下架状态（管理员）
     * 说明：status=1 发布，status=2 下线；重新发布时刷新发布时间
     *
     * @param articleId 文章ID
     * @param status    目标状态（1-已发布 2-已下线）
     */
    public void changeStatus(String articleId, Integer status) {
        // 1. 校验文章是否存在
        if (knowledgeArticleMapper.selectById(articleId) == null) {
            throw new BusinessException("文章不存在");
        }
        // 2. 构建状态更新
        KnowledgeArticle article = new KnowledgeArticle();
        article.setId(articleId);
        article.setStatus(status);
        article.setUpdatedAt(LocalDateTime.now());
        // 3. 若改为已发布状态，同步刷新发布时间
        if (status != null && status == 1) {
            article.setPublishedAt(LocalDateTime.now());
        }
        // 4. 执行更新
        knowledgeArticleMapper.updateById(article);
    }

    /**
     * 删除文章（管理员）
     *
     * @param articleId 文章ID
     */
    public void deleteArticle(String articleId) {
        // 1. 校验文章是否存在
        if (knowledgeArticleMapper.selectById(articleId) == null) {
            throw new BusinessException("文章不存在");
        }
        // 2. 物理删除
        knowledgeArticleMapper.deleteById(articleId);
    }
}
