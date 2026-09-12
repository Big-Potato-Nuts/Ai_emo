package org.example.AiSpringboot.DTO.Response;

// 后台数据总览响应 DTO

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 后台数据看板总览
 * 结构对应 dashboard.vue 的三个图表 + 四个统计卡片：
 * 1. systemOverview     顶部四个统计卡片（总用户/情绪日志/咨询会话/平均情绪）
 * 2. consultationStats  咨询会话统计卡片 + 每日咨询趋势图
 * 3. emotionTrend       情绪趋势分析图
 * 4. userActivity       用户活跃度趋势图
 */
@Data
@Builder
public class AnalyticsOverviewDTO {

    // 系统总览（顶部卡片）
    private SystemOverview systemOverview;

    // 咨询会话统计（含每日趋势）
    private ConsultationStats consultationStats;

    // 近7天情绪趋势
    private List<EmotionTrendItem> emotionTrend;

    // 近7天用户活跃度
    private List<UserActivityItem> userActivity;

    /**
     * 系统总览（顶部四个统计卡片数据）
     */
    @Data
    @Builder
    public static class SystemOverview {
        // 总用户数
        private Long totalUsers;
        // 活跃用户数（近7天有会话的用户）
        private Long activeUsers;
        // 情绪日记总数
        private Long totalDiaries;
        // 今日新增日记数
        private Long todayNewDiaries;
        // 咨询会话总数
        private Long totalSessions;
        // 今日新增会话数
        private Long todayNewSessions;
        // 平均情绪评分（0-10）
        private BigDecimal avgMoodScore;
    }

    /**
     * 咨询会话统计
     */
    @Data
    @Builder
    public static class ConsultationStats {
        // 总会话数
        private Long totalSessions;
        // 平均会话时长（分钟）
        private BigDecimal avgDurationMinutes;
        // 每日咨询趋势（近7天）
        private List<ConsultationDailyItem> dailyTrend;
    }

    /**
     * 每日咨询趋势项（咨询活动统计图）
     */
    @Data
    @Builder
    public static class ConsultationDailyItem {
        // 日期（yyyy-MM-dd）
        private String date;
        // 当日会话数
        private Long sessionCount;
        // 当日参与用户数
        private Long userCount;
    }

    /**
     * 每日情绪趋势项（情绪趋势分析图）
     */
    @Data
    @Builder
    public static class EmotionTrendItem {
        // 日期
        private String date;
        // 当日平均情绪评分
        private BigDecimal avgMoodScore;
        // 当日日记记录数
        private Long recordCount;
    }

    /**
     * 每日用户活跃度项（用户活跃度趋势图）
     */
    @Data
    @Builder
    public static class UserActivityItem {
        // 日期
        private String date;
        // 当日活跃用户数
        private Long activeUsers;
        // 当日新增用户数
        private Long newUsers;
        // 当日写日记用户数
        private Long diaryUsers;
        // 当日咨询用户数
        private Long consultationUsers;
    }
}
