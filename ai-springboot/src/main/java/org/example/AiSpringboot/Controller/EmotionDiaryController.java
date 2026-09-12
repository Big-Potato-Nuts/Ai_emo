package org.example.AiSpringboot.Controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.DTO.Command.EmotionDiaryCreatDTO;
import org.example.AiSpringboot.DTO.Response.EmotionDiaryAdminResponseDTO;
import org.example.AiSpringboot.Entity.EmotionDiary;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Service.EmotionDiaryService;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.example.AiSpringboot.enumClass.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 情绪日记控制器
 * 提供情绪日记相关的 3 个接口：
 * 1. 新增日记（普通用户，当天只能记一条）
 * 2. 管理端分页查询（管理员，支持按用户/评分区间筛选）
 * 3. 管理端删除（管理员）
 */
@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {

    @Autowired
    private EmotionDiaryService emotionDiaryService;

    /**
     * 新增情绪日记（登录用户可用）
     * 请求体字段：diaryDate、moodScore(1-10)、dominantEmotion、emotionTriggers、diaryContent、sleepQuality、stressLevel
     *
     * @param dto 日记内容（带参数校验注解）
     * @return 保存后的日记
     */
    @PostMapping
    public Result<EmotionDiary> addDiary(@Valid @RequestBody EmotionDiaryCreatDTO dto) {
        // 1. 从 JWT 解析当前用户ID（日记归属当前用户）
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();
        // 2. 调用服务层新增（内部校验当天是否已记录）
        return Result.ok(emotionDiaryService.addDiary(userId, dto));
    }

    /**
     * 管理端分页查询全部用户的日记
     * 说明：分页参数兼容 current/size（前端 emotional.vue 写法）
     *
     * @param current       页码
     * @param size          每页条数
     * @param userId        按用户ID筛选（可选）
     * @param moodScreRange 按评分区间筛选，如 "1-3"/"4-6"/"7-10"（可选）
     * @return 日记分页结果（records 含 username/nickname/aiEmotionAnalysis 等）
     */
    @GetMapping("/admin/page")
    public Result<IPage<EmotionDiaryAdminResponseDTO>> adminPage(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String moodScreRange) {
        // 1. 校验管理员权限（普通用户不可访问管理端接口）
        checkAdmin();
        // 2. 统一分页参数，默认第1页、每页10条
        long pageNum = current != null ? current : 1L;
        long pageSize = size != null ? size : 10L;
        // 3. 调用服务层查询并返回
        return Result.ok(emotionDiaryService.pageAdmin(pageNum, pageSize, userId, moodScreRange));
    }

    /**
     * 管理端删除日记
     *
     * @param id 日记ID
     * @return 操作结果
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id) {
        // 1. 校验管理员权限
        checkAdmin();
        // 2. 调用服务层删除
        emotionDiaryService.deleteDiary(id);
        return Result.ok();
    }

    /**
     * 校验当前用户是否为管理员（非管理员直接抛出业务异常，由全局异常处理器统一返回）
     */
    private void checkAdmin() {
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Integer roleType = jwt.getClaim("roleType").asInt();
        if (!UserType.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException("无权限访问，仅管理员可操作");
        }
    }
}
