package org.example.AiSpringboot.Mapper;

// 数据分析 Mapper 接口（纯统计查询，无对应实体表）

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 数据分析 Mapper
 * 提供后台数据看板所需的各类统计 SQL（都是只读聚合查询）
 */
@Mapper
public interface DataAnalyticsMapper {

    /**
     * 查询用户总数
     * @return 用户表记录总数
     */
    @Select("SELECT COUNT(*) FROM user")
    long countTotalUsers();

    /**
     * 查询活跃用户数
     * 口径：最近 7 天内有发起咨询会话的用户数（去重）
     * @return 活跃用户数量
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM consultation_session WHERE started_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    long countActiveUsers();

    /**
     * 查询情绪日记总数
     * @return 日记表记录总数
     */
    @Select("SELECT COUNT(*) FROM emotion_diary")
    long countTotalDiaries();

    /**
     * 查询今日新增情绪日记数
     * 口径：diary_date = 今天的记录数
     * @return 今日日记数量
     */
    @Select("SELECT COUNT(*) FROM emotion_diary WHERE diary_date = CURDATE()")
    long countTodayNewDiaries();

    /**
     * 查询咨询会话总数
     * @return 会话表记录总数
     */
    @Select("SELECT COUNT(*) FROM consultation_session")
    long countTotalSessions();

    /**
     * 查询今日新增会话数
     * 口径：started_at 属于今天的会话数
     * @return 今日会话数量
     */
    @Select("SELECT COUNT(*) FROM consultation_session WHERE started_at >= CURDATE()")
    long countTodayNewSessions();

    /**
     * 查询平均情绪评分
     * 口径：所有日记 mood_score 的平均值（1-10 分）
     * @return 平均分，可能为 null（无数据时）
     */
    @Select("SELECT AVG(mood_score) FROM emotion_diary")
    BigDecimal avgMoodScore();

    /**
     * 查询会话平均时长（分钟）
     * 口径：对每个会话用"最后一条消息时间 - 第一条消息时间"估算会话时长，再取平均
     * 说明：MySQL 不允许 AVG 内直接嵌套 MIN/MAX 聚合函数，所以先用子查询算出每个会话的时长，外层再取平均
     * @return 平均时长（分钟），可能为 null（无数据时）
     */
    @Select("SELECT AVG(t.duration_minutes) FROM (" +
            "  SELECT TIMESTAMPDIFF(MINUTE, MIN(created_at), MAX(created_at)) AS duration_minutes " +
            "  FROM consultation_message GROUP BY session_id" +
            ") t")
    BigDecimal avgSessionDurationMinutes();

    /**
     * 查询近 7 天每日咨询趋势
     * 返回每条记录字段：date（日期）、sessionCount（会话数）、userCount（参与用户数）
     * 说明：按自然日分组，不足 7 天数据的天数可能不出现，由后端补齐为 0
     * @return 每日咨询统计列表
     */
    @Select("SELECT DATE(started_at) AS date, COUNT(*) AS sessionCount, COUNT(DISTINCT user_id) AS userCount " +
            "FROM consultation_session " +
            "WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(started_at) ORDER BY date")
    List<Map<String, Object>> selectDailyConsultationTrend();

    /**
     * 查询近 7 天每日情绪趋势
     * 返回每条记录字段：date（日期）、avgMoodScore（平均评分）、recordCount（记录数）
     * @return 每日情绪统计列表
     */
    @Select("SELECT DATE(created_at) AS date, ROUND(AVG(mood_score), 1) AS avgMoodScore, COUNT(*) AS recordCount " +
            "FROM emotion_diary " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> selectEmotionTrend();

    /**
     * 查询近 7 天每日用户活跃度
     * 返回每条记录字段：
     *   date               日期
     *   activeUsers        当日活跃用户数（有会话或有日记的用户，去重）
     *   newUsers           当日新增注册用户数
     *   diaryUsers         当日写日记的用户数（去重）
     *   consultationUsers  当日发起会话的用户数（去重）
     * 说明：活跃用户 = 会话用户 UNION ALL 日记用户，用真实 user_id 去重；
     *       新增用户按 user.created_at 归属日期统计，避免把统计对象错记成序列占位值
     * @return 每日活跃度统计列表
     */
    @Select("SELECT d.date, " +
            "       COUNT(DISTINCT act.uid) AS activeUsers, " +
            "       COUNT(DISTINCT nu.id) AS newUsers, " +
            "       COUNT(DISTINCT di.uid) AS diaryUsers, " +
            "       COUNT(DISTINCT cs.uid) AS consultationUsers " +
            "FROM (SELECT CURDATE() - INTERVAL seq DAY AS date FROM (" +
            "      SELECT 0 AS seq UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 " +
            "      UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) numbers) d " +
            "LEFT JOIN (" +
            "      SELECT DATE(started_at) AS dt, user_id AS uid FROM consultation_session " +
            "        WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "      UNION ALL " +
            "      SELECT diary_date AS dt, user_id AS uid FROM emotion_diary " +
            "        WHERE diary_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)" +
            ") act ON act.dt = d.date " +
            "LEFT JOIN (SELECT DATE(created_at) AS dt, id FROM user) nu ON nu.dt = d.date " +
            "LEFT JOIN (SELECT diary_date AS dt, user_id AS uid FROM emotion_diary " +
            "        WHERE diary_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)) di ON di.dt = d.date " +
            "LEFT JOIN (SELECT DATE(started_at) AS dt, user_id AS uid FROM consultation_session " +
            "        WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)) cs ON cs.dt = d.date " +
            "GROUP BY d.date ORDER BY d.date")
    List<Map<String, Object>> selectUserActivityTrend();
}
