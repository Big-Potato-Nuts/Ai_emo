package org.example.AiSpringboot.Service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.AiSpringboot.DTO.Command.ConsultationSessionCreatDTO;
import org.example.AiSpringboot.DTO.Response.SessionEmotionResponseDTO;
import org.example.AiSpringboot.DTO.Response.SessionPageResponseDTO;
import org.example.AiSpringboot.Entity.ConsultationSession;
import org.example.AiSpringboot.Entity.User;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Mapper.ConsultationSessionMapper;
import org.example.AiSpringboot.Mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 咨询会话服务
 * 职责：
 * 1. 创建新会话（原有逻辑）
 * 2. 分页查询会话列表（前台查自己的、后台查全部）
 * 3. 删除会话（普通用户只能删自己的，管理员可删任意）
 * 4. 查询会话的情绪分析结果（读取 last_emotion_analysis JSON）
 */
@Service
public class ConsultationSessionService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    /**
     * 创建新会话（原有方法）
     * 说明：会话标题为空时自动生成"宁渡AI助手 + 时间"的默认标题
     *
     * @param userId  创建者用户ID
     * @param creatDTO 创建参数（会话标题、初始消息）
     * @return 创建成功的会话实体，用户不存在时返回 null
     */
    public ConsultationSession createSession(Long userId, ConsultationSessionCreatDTO creatDTO) {
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 2. 构建会话实体（标题、用户、开始时间）
            ConsultationSession session = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(creatDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            // 3. 标题为空时生成默认标题
            if (StrUtil.isBlank(creatDTO.getSessionTitle())) {
                session.setSessionTitle("宁渡AI助手" + DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
            }
            // 4. 插入数据库
            consultationSessionMapper.insert(session);
            return session;
        }
        return null;
    }

    /**
     * 分页查询会话列表
     * 说明：根据当前用户角色决定查询范围——普通用户只查自己的会话，管理员查全部用户会话
     *
     * @param currentUserId 当前登录用户ID（用于普通用户过滤）
     * @param isAdmin       当前用户是否为管理员（管理员查全部）
     * @param pageNum       页码（从1开始）
     * @param pageSize      每页条数
     * @return 会话分页结果（含用户昵称、消息数、最后消息摘要）
     */
    public IPage<SessionPageResponseDTO> pageSessions(Long currentUserId, boolean isAdmin, long pageNum, long pageSize) {
        // 1. 构造分页对象
        Page<SessionPageResponseDTO> page = new Page<>(pageNum, pageSize);
        // 2. 管理员传 null 查全部；普通用户传自己的 ID 做过滤
        Long queryUserId = isAdmin ? null : currentUserId;
        // 3. 执行联表分页查询（SQL 见 ConsultationSessionMapper）
        return consultationSessionMapper.selectSessionPage(page, queryUserId);
    }

    /**
     * 删除会话
     * 说明：先查询会话归属，普通用户只能删除属于自己的会话，管理员可删除任意会话
     *
     * @param sessionId     要删除的会话ID
     * @param currentUserId 当前登录用户ID
     * @param isAdmin       当前用户是否为管理员
     */
    public void deleteSession(Long sessionId, Long currentUserId, boolean isAdmin) {
        // 1. 查询会话是否存在
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        // 2. 权限校验：非管理员且不是本人创建的会话，禁止删除
        if (!isAdmin && !session.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权删除他人的会话");
        }
        // 3. 执行删除（外键 ON DELETE CASCADE 会级联删除该会话下的所有消息）
        consultationSessionMapper.deleteById(sessionId);
    }

    /**
     * 校验会话归属（查看会话详情前的权限校验）
     * 说明：普通用户只能查看属于自己的会话，管理员可查看任意会话
     *
     * @param sessionId     会话ID
     * @param currentUserId 当前登录用户ID
     * @param isAdmin       当前用户是否为管理员
     */
    public void checkSessionOwner(Long sessionId, Long currentUserId, boolean isAdmin) {
        // 1. 查询会话是否存在
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        // 2. 权限校验：非管理员且会话不属于当前用户时禁止查看
        if (!isAdmin && !session.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权查看他人的会话");
        }
    }

    /**
     * 查询会话情绪分析结果
     * 说明：情绪数据存在会话表的 last_emotion_analysis（JSON 字符串）中；
     *      若还没有情绪分析数据，则返回一组中性默认值，保证前端"情绪花园"卡片始终可渲染
     *
     * @param sessionId 会话ID（支持传入 "session_22" 或纯数字 "22" 两种格式）
     * @return 情绪分析结果 DTO
     */
    public SessionEmotionResponseDTO getSessionEmotion(String sessionId) {
        // 1. 解析会话ID：去掉 "session_" 前缀并转为数字
        String idStr = sessionId.startsWith("session_") ? sessionId.substring("session_".length()) : sessionId;
        Long id;
        try {
            id = Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            throw new BusinessException("会话ID格式不正确");
        }
        // 2. 查询会话
        ConsultationSession session = consultationSessionMapper.selectById(id);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        // 3. 读取情绪分析 JSON；为空时使用中性默认值
        String analysisJson = session.getLastEmotionAnalysis();
        if (StrUtil.isBlank(analysisJson)) {
            // 默认：中性情绪，评分50，无风险，状态平稳
            return SessionEmotionResponseDTO.builder()
                    .primaryEmotion("中性")
                    .emotionScore(50)
                    .isNegative(false)
                    .riskLevel(0)
                    .suggestion("情绪状态平稳")
                    .riskDescription("")
                    .improvementSuggestions(new ArrayList<>())
                    .build();
        }
        // 4. 解析 JSON 并转换为响应 DTO
        JSONObject json = JSONUtil.parseObj(analysisJson);
        return SessionEmotionResponseDTO.builder()
                .primaryEmotion(json.getStr("primaryEmotion", "中性"))
                .emotionScore(json.getInt("emotionScore", 50))
                .isNegative(json.getBool("isNegative", false))
                .riskLevel(json.getInt("riskLevel", 0))
                .suggestion(json.getStr("suggestion", "情绪状态平稳"))
                .riskDescription(json.getStr("riskDescription", ""))
                .improvementSuggestions(json.getBeanList("improvementSuggestions", String.class))
                .build();
    }
}
