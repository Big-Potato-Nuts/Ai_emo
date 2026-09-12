package org.example.AiSpringboot.Controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.AiSpringboot.Common.Result;
import org.example.AiSpringboot.DTO.Response.ConsultationMessageResponseDTO;
import org.example.AiSpringboot.DTO.Response.SessionEmotionResponseDTO;
import org.example.AiSpringboot.DTO.Response.SessionPageResponseDTO;
import org.example.AiSpringboot.Service.ConsultationMessageService;
import org.example.AiSpringboot.Service.ConsultationSessionService;
import org.example.AiSpringboot.Util.JwtTokenUtil;
import org.example.AiSpringboot.enumClass.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咨询会话管理控制器
 * 提供会话相关的 4 个接口：
 * 1. 会话列表（前台查自己的 / 后台查全部）
 * 2. 会话消息列表（会话详情）
 * 3. 删除会话
 * 4. 会话情绪分析
 */
@RestController
@RequestMapping("/api/psychological-chat")
public class ConsultationSessionController {

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    /**
     * 分页查询会话列表
     * 说明：兼容前端三种分页参数写法——pageNum/pageSize（前台会话页）、currentPage/size（后台）、current/size（通用）
     *
     * @param pageNum    页码（可为空）
     * @param pageSize   每页条数（可为空）
     * @param currentPage 页码别名（currentPage/size 写法时使用）
     * @param current     页码别名（current/size 写法时使用）
     * @param size        每页条数别名
     * @return 会话分页结果（records 含 id、sessionTitle、userNickname、messageCount、lastMessageContent、lastMessageTime）
     */
    @GetMapping("/sessions")
    public Result<IPage<SessionPageResponseDTO>> pageSessions(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(required = false) Long currentPage,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long size) {
        // 1. 从 JWT 解析当前用户信息（过滤器已保证 token 有效）
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();
        Integer roleType = jwt.getClaim("roleType").asInt();
        // 2. 判断是否管理员（管理员可查全部用户会话，普通用户只查自己的）
        boolean isAdmin = UserType.ADMIN.getCode().equals(roleType);

        // 3. 统一分页参数：优先用 pageNum/pageSize，为空时回退到 currentPage/current 与 size
        long pageCurrent = pageNum != null ? pageNum : (currentPage != null ? currentPage : (current != null ? current : 1L));
        long pageSizeValue = pageSize != null ? pageSize : (size != null ? size : 10L);

        // 4. 调用服务层分页查询并返回
        return Result.ok(consultationSessionService.pageSessions(userId, isAdmin, pageCurrent, pageSizeValue));
    }

    /**
     * 查询会话的消息列表（会话详情）
     * 说明：直接返回消息数组（不包 records 分页壳），时间正序
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageResponseDTO>> sessionMessages(@PathVariable Long sessionId) {
        // 1. 从 JWT 解析当前用户（用于归属校验，普通用户不能查看他人会话）
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();
        Integer roleType = jwt.getClaim("roleType").asInt();
        boolean isAdmin = UserType.ADMIN.getCode().equals(roleType);

        // 2. 校验归属：会话不属于当前用户且当前用户不是管理员时拒绝访问
        consultationSessionService.checkSessionOwner(sessionId, userId, isAdmin);
        // 3. 查询并返回消息列表
        return Result.ok(consultationMessageService.listMessagesBySessionId(sessionId));
    }

    /**
     * 删除会话
     * 说明：普通用户只能删除自己的会话，管理员可删除任意会话；删除后级联删除其下消息
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        // 1. 解析当前用户
        DecodedJWT jwt = JwtTokenUtil.verifyToken(JwtTokenUtil.getCurrentToken());
        Long userId = jwt.getClaim("userId").asLong();
        Integer roleType = jwt.getClaim("roleType").asInt();
        boolean isAdmin = UserType.ADMIN.getCode().equals(roleType);
        // 2. 调用服务层删除（内部做归属校验）
        consultationSessionService.deleteSession(sessionId, userId, isAdmin);
        return Result.ok();
    }

    /**
     * 查询会话情绪分析结果
     * 说明：sessionId 兼容 "session_22" 和纯数字 "22" 两种格式
     *
     * @param sessionId 会话ID（可带 session_ 前缀）
     * @return 情绪分析结果（无数据时返回中性默认值）
     */
    @GetMapping("/session/{sessionId}/emotion")
    public Result<SessionEmotionResponseDTO> sessionEmotion(@PathVariable String sessionId) {
        return Result.ok(consultationSessionService.getSessionEmotion(sessionId));
    }
}
