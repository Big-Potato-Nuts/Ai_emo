package org.example.AiSpringboot.Service;

import org.example.AiSpringboot.DTO.Response.AnalyticsOverviewDTO;
import org.example.AiSpringboot.Mapper.DataAnalyticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据分析服务
 * 职责：聚合各统计查询结果，组装成后台数据看板所需的完整总览数据
 */
@Service
public class DataAnalyticsService {

    @Autowired
    private DataAnalyticsMapper dataAnalyticsMapper;

    /**
     * 查询后台数据总览
     * 说明：包含系统总览卡片、咨询会话统计、情绪趋势、用户活跃度四大块，
     *      所有日期趋势统一补齐最近 7 天（无数据的天补 0），保证前端图表连续
     *
     * @return 数据总览 DTO
     */
    public AnalyticsOverviewDTO overview() {
        // 1. 组装系统总览（顶部四个统计卡片）
        AnalyticsOverviewDTO.SystemOverview systemOverview = AnalyticsOverviewDTO.SystemOverview.builder()
                .totalUsers(dataAnalyticsMapper.countTotalUsers())
                .activeUsers(dataAnalyticsMapper.countActiveUsers())
                .totalDiaries(dataAnalyticsMapper.countTotalDiaries())
                .todayNewDiaries(dataAnalyticsMapper.countTodayNewDiaries())
                .totalSessions(dataAnalyticsMapper.countTotalSessions())
                .todayNewSessions(dataAnalyticsMapper.countTodayNewSessions())
                .avgMoodScore(defaultZero(dataAnalyticsMapper.avgMoodScore()))
                .build();

        // 2. 组装咨询会话统计（总数 + 平均时长 + 近7天每日趋势）
        AnalyticsOverviewDTO.ConsultationStats consultationStats = AnalyticsOverviewDTO.ConsultationStats.builder()
                .totalSessions(dataAnalyticsMapper.countTotalSessions())
                .avgDurationMinutes(defaultZero(dataAnalyticsMapper.avgSessionDurationMinutes()))
                .dailyTrend(buildDailyConsultationTrend())
                .build();

        // 3. 组装情绪趋势（近7天，按日期补 0）
        List<AnalyticsOverviewDTO.EmotionTrendItem> emotionTrend = buildEmotionTrend();

        // 4. 组装用户活跃度（近7天，按日期补 0）
        List<AnalyticsOverviewDTO.UserActivityItem> userActivity = buildUserActivity();

        // 5. 汇总返回
        return AnalyticsOverviewDTO.builder()
                .systemOverview(systemOverview)
                .consultationStats(consultationStats)
                .emotionTrend(emotionTrend)
                .userActivity(userActivity)
                .build();
    }

    /**
     * 构建近 7 天每日咨询趋势（无数据的天补 0）
     */
    private List<AnalyticsOverviewDTO.ConsultationDailyItem> buildDailyConsultationTrend() {
        // 1. 查询数据库已有的聚合结果
        List<Map<String, Object>> rows = dataAnalyticsMapper.selectDailyConsultationTrend();
        // 2. 转成 date -> 统计项的映射，方便按日期补齐
        Map<String, Map<String, Object>> byDate = rows.stream()
                .collect(Collectors.toMap(r -> String.valueOf(r.get("date")), r -> r));
        // 3. 遍历最近 7 天，逐天取数，没有的天补 0
        List<AnalyticsOverviewDTO.ConsultationDailyItem> result = new ArrayList<>();
        for (LocalDate d = LocalDate.now().minusDays(6); !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            String dateStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<String, Object> row = byDate.get(dateStr);
            long sessionCount = row != null ? ((Number) row.get("sessionCount")).longValue() : 0L;
            long userCount = row != null ? ((Number) row.get("userCount")).longValue() : 0L;
            result.add(AnalyticsOverviewDTO.ConsultationDailyItem.builder()
                    .date(dateStr).sessionCount(sessionCount).userCount(userCount).build());
        }
        return result;
    }

    /**
     * 构建近 7 天情绪趋势（无数据的天补 0）
     */
    private List<AnalyticsOverviewDTO.EmotionTrendItem> buildEmotionTrend() {
        List<Map<String, Object>> rows = dataAnalyticsMapper.selectEmotionTrend();
        Map<String, Map<String, Object>> byDate = rows.stream()
                .collect(Collectors.toMap(r -> String.valueOf(r.get("date")), r -> r));
        List<AnalyticsOverviewDTO.EmotionTrendItem> result = new ArrayList<>();
        for (LocalDate d = LocalDate.now().minusDays(6); !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            String dateStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<String, Object> row = byDate.get(dateStr);
            BigDecimal avgScore = row != null ? new BigDecimal(String.valueOf(row.get("avgMoodScore"))) : BigDecimal.ZERO;
            long recordCount = row != null ? ((Number) row.get("recordCount")).longValue() : 0L;
            result.add(AnalyticsOverviewDTO.EmotionTrendItem.builder()
                    .date(dateStr).avgMoodScore(avgScore).recordCount(recordCount).build());
        }
        return result;
    }

    /**
     * 构建近 7 天用户活跃度（无数据的天补 0）
     */
    private List<AnalyticsOverviewDTO.UserActivityItem> buildUserActivity() {
        List<Map<String, Object>> rows = dataAnalyticsMapper.selectUserActivityTrend();
        Map<String, Map<String, Object>> byDate = rows.stream()
                .collect(Collectors.toMap(r -> String.valueOf(r.get("date")), r -> r));
        List<AnalyticsOverviewDTO.UserActivityItem> result = new ArrayList<>();
        for (LocalDate d = LocalDate.now().minusDays(6); !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            String dateStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<String, Object> row = byDate.get(dateStr);
            result.add(AnalyticsOverviewDTO.UserActivityItem.builder()
                    .date(dateStr)
                    .activeUsers(row != null ? ((Number) row.get("activeUsers")).longValue() : 0L)
                    .newUsers(row != null ? ((Number) row.get("newUsers")).longValue() : 0L)
                    .diaryUsers(row != null ? ((Number) row.get("diaryUsers")).longValue() : 0L)
                    .consultationUsers(row != null ? ((Number) row.get("consultationUsers")).longValue() : 0L)
                    .build());
        }
        return result;
    }

    /**
     * 空值兜底：聚合结果可能为 null（无数据时），统一转 0
     */
    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
