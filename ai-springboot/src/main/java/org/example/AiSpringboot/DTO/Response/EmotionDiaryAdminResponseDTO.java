package org.example.AiSpringboot.DTO.Response;

// 后台情绪日记分页项响应 DTO

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 后台情绪日记分页项
 * 字段与 emotional.vue 表格 + 详情弹窗完全对应（含联表补出的用户名、昵称）
 */
@Data
public class EmotionDiaryAdminResponseDTO {

    // 日记ID
    private Long id;

    // 用户ID
    private Long userId;

    // 用户名（联表查出）
    private String username;

    // 昵称（联表查出）
    private String nickname;

    // 日记日期
    private LocalDate diaryDate;

    // 情绪评分（1-10）
    private Integer moodScore;

    // 主要情绪
    private String dominantEmotion;

    // 情绪触发因素
    private String emotionTriggers;

    // 日记内容
    private String diaryContent;

    // 睡眠质量（1-5）
    private Integer sleepQuality;

    // 压力水平（1-5）
    private Integer stressLevel;

    // AI 情绪分析结果（JSON 字符串，前端解析后展示）
    private String aiEmotionAnalysis;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;
}
