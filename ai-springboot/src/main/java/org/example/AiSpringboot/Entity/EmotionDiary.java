package org.example.AiSpringboot.Entity;

// 情绪日记实体类，对应数据库表 emotion_diary

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情绪日记实体
 * 对应表：emotion_diary（用户每日情绪记录，一个用户一天最多一条）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("emotion_diary")
public class EmotionDiary {

    // 日记ID（主键，数据库自增）
    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户ID（关联 user 表，外键）
    @TableField("user_id")
    private Long userId;

    // 日记日期（与 user_id 组成唯一约束，同一天只能记一条）
    @TableField("diary_date")
    private LocalDate diaryDate;

    // 情绪评分（1-10 分，10 分表示情绪最好）
    @TableField("mood_score")
    private Integer moodScore;

    // 主要情绪（如：开心、平静、焦虑、悲伤、兴奋、疲惫、惊讶、困惑）
    @TableField("dominant_emotion")
    private String dominantEmotion;

    // 情绪触发因素（用户填写的：今天什么事情影响了情绪）
    @TableField("emotion_triggers")
    private String emotionTriggers;

    // 日记内容（用户填写的今日感想）
    @TableField("diary_content")
    private String diaryContent;

    // 睡眠质量（1-5 分，5 表示最好）
    @TableField("sleep_quality")
    private Integer sleepQuality;

    // 压力水平（1-5 分，5 表示压力最大）
    @TableField("stress_level")
    private Integer stressLevel;

    // AI 情绪分析结果（JSON 字符串，由 AI 生成的情绪分析结论）
    @TableField("ai_emotion_analysis")
    private String aiEmotionAnalysis;

    // AI 分析更新时间
    @TableField("ai_analysis_updated_at")
    private LocalDateTime aiAnalysisUpdatedAt;

    // 创建时间（数据库默认当前时间）
    @TableField("created_at")
    private LocalDateTime createdAt;

    // 更新时间（数据库自动更新）
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
