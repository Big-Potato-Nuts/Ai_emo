package org.example.AiSpringboot.Controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.DTO.Command.ArticleStatusDTO;
import org.example.AiSpringboot.DTO.Command.KnowledgeArticleCreatDTO;
import org.example.AiSpringboot.DTO.Response.KnowledgeArticleResponseDTO;
import org.example.AiSpringboot.DTO.Response.KnowledgeCategoryResponseDTO;
import org.example.AiSpringboot.Entity.KnowledgeArticle;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Service.KnowledgeArticleService;
import org.example.AiSpringboot.Service.KnowledgeCategoryService;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.example.AiSpringboot.enumClass.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库控制器
 * 提供知识库相关的 7 个接口：
 * 1. 分类树（所有登录用户）
 * 2. 文章分页（前台只查已发布，后台查全部）
 * 3. 文章详情（阅读数 +1）
 * 4. 新增文章（管理员）
 * 5. 更新文章（管理员）
 * 6. 文章上下架（管理员）
 * 7. 删除文章（管理员）
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeCategoryService knowledgeCategoryService;

    @Autowired
    private KnowledgeArticleService knowledgeArticleService;

    /**
     * 查询知识分类树（登录用户可用）
     * 说明：返回数组，每条含 id、categoryName、children 等
     *
     * @return 分类树列表
     */
    @GetMapping("/category/tree")
    public Result<List<KnowledgeCategoryResponseDTO>> categoryTree() {
        return Result.ok(knowledgeCategoryService.categoryTree());
    }

    /**
     * 分页查询文章列表
     * 说明：
     * - 前台用户端（普通用户）只返回已发布文章，默认按发布时间倒序
     * - 后台管理端（管理员）返回全部状态文章，可传 status 筛选
     * - 分页参数兼容 currentPage/size 与 current/size 两种写法
     *
     * @param currentPage   页码（别名 current）
     * @param size          每页条数
     * @param current       页码别名
     * @param title         标题模糊搜索（可选）
     * @param categoryId    分类ID筛选（可选）
     * @param status        状态筛选（后台，可选）
     * @param sortField     排序字段（publishedAt/updatedAt/createdAt/readCount，可选）
     * @param sortDirection 排序方向（desc/asc，可选）
     * @return 文章分页结果
     */
    @GetMapping("/article/page")
    public Result<IPage<KnowledgeArticleResponseDTO>> articlePage(
            @RequestParam(required = false) Long currentPage,
            @RequestParam(required = false) Long size,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {
        // 1. 解析当前用户角色（决定查询范围：管理员查全部，普通用户只看已发布）
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Integer roleType = jwt.getClaim("roleType").asInt();
        boolean isAdmin = UserType.ADMIN.getCode().equals(roleType);
        // 2. 统一分页参数
        long pageNum = currentPage != null ? currentPage : (current != null ? current : 1L);
        long pageSize = size != null ? size : 10L;
        // 3. 调用服务层查询并返回
        return Result.ok(knowledgeArticleService.pageArticles(
                pageNum, pageSize, title, categoryId, status, isAdmin, sortField, sortDirection));
    }

    /**
     * 查询文章详情（登录用户可用，阅读数 +1）
     *
     * @param id 文章ID（UUID）
     * @return 文章详情（含 categoryName、authorName、tagArray）
     */
    @GetMapping("/article/{id}")
    public Result<KnowledgeArticleResponseDTO> articleDetail(@PathVariable String id) {
        return Result.ok(knowledgeArticleService.getArticleDetail(id));
    }

    /**
     * 新增文章（管理员）
     * 说明：新增即发布（status=1）；请求体含前端生成的 id（UUID）
     *
     * @param dto 文章内容
     * @return 新建后的文章
     */
    @PostMapping("/article")
    public Result<KnowledgeArticle> createArticle(@Valid @RequestBody KnowledgeArticleCreatDTO dto) {
        // 1. 校验管理员权限
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();
        checkAdmin(jwt);
        // 2. 调用服务层创建（作者为当前管理员）
        return Result.ok(knowledgeArticleService.createArticle(dto, userId));
    }

    /**
     * 更新文章（管理员）
     * 说明：只更新编辑字段，不覆盖阅读数、作者、状态
     *
     * @param id  文章ID
     * @param dto 编辑后的内容
     * @return 操作结果
     */
    @PutMapping("/article/{id}")
    public Result<Void> updateArticle(@PathVariable String id, @Valid @RequestBody KnowledgeArticleCreatDTO dto) {
        // 1. 校验管理员权限
        checkAdmin(JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken()));
        // 2. 调用服务层更新
        knowledgeArticleService.updateArticle(id, dto);
        return Result.ok();
    }

    /**
     * 修改文章上下架状态（管理员）
     * 说明：请求体 {status: 1} 发布，{status: 2} 下线
     *
     * @param id   文章ID
     * @param dto  目标状态
     * @return 操作结果
     */
    @PutMapping("/article/{id}/status")
    public Result<Void> changeArticleStatus(@PathVariable String id, @Valid @RequestBody ArticleStatusDTO dto) {
        // 1. 校验管理员权限
        checkAdmin(JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken()));
        // 2. 调用服务层修改状态
        knowledgeArticleService.changeStatus(id, dto.getStatus());
        return Result.ok();
    }

    /**
     * 删除文章（管理员）
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable String id) {
        // 1. 校验管理员权限
        checkAdmin(JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken()));
        // 2. 调用服务层删除
        knowledgeArticleService.deleteArticle(id);
        return Result.ok();
    }

    /**
     * 校验当前用户是否为管理员（非管理员抛出业务异常）
     */
    private void checkAdmin(DecodedJWT jwt) {
        Integer roleType = jwt.getClaim("roleType").asInt();
        if (!UserType.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException("无权限访问，仅管理员可操作");
        }
    }
}
