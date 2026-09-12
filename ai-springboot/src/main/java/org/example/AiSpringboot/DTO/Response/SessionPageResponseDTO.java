package org.example.AiSpringboot.DTO.Response;

// 会话分页列表项响应 DTO

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询会话分页列表项
 * 供前台会话列表和后台咨询记录列表共用：
 * 前台需要 id / sessionTitle；后台需要 userNickname / messageCount / lastMessageContent / lastMessageTime
 */
@Data
public class SessionPageResponseDTO {

    // 会话ID（数字主键）
    private Long id;

    // 所属用户ID
    private Long userId;

    // 用户昵称（联表查出，取昵称，昵称为空时回退为用户名）
    private String userNickname;

    // 会话标题
    private String sessionTitle;

    // 会话开始时间
    private LocalDateTime startedAt;

    // 消息总数（子查询统计）
    private Long messageCount;

    // 最后一条消息内容（子查询取最新，用于列表摘要预览）
    private String lastMessageContent;

    // 最后一条消息时间（子查询取最新）
    private LocalDateTime lastMessageTime;

    // 最后一次情绪分析结果（JSON 字符串，列表暂不解析，详情使用）
    private String lastEmotionAnalysis;

    // 最后一次情绪分析更新时间
    private LocalDateTime lastEmotionUpdatedAt;
}
