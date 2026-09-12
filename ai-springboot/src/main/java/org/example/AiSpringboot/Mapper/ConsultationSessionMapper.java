package org.example.AiSpringboot.Mapper;

// 咨询会话 Mapper 接口，对应表 consultation_session

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.AiSpringboot.DTO.Response.SessionPageResponseDTO;
import org.example.AiSpringboot.Entity.ConsultationSession;

/**
 * 咨询会话 Mapper
 * 继承 BaseMapper 后自动获得会话表的基础增删改查能力；
 * 额外提供联表分页查询（带用户昵称、消息数、最后一条消息摘要，供列表页展示）
 */
@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSession> {

    /**
     * 分页查询会话列表（联表带出用户昵称和消息统计）
     * 说明：
     * 1. LEFT JOIN user 表，带出用户昵称（前台展示"用户名"、后台展示归属用户）
     * 2. 通过子查询统计每个会话的消息数、最后一条消息内容和时间
     * 3. userId 为 null 时查询全部用户的会话（管理员后台使用）；否则只查指定用户的会话
     *
     * @param page   MyBatis-Plus 分页对象
     * @param userId 用户ID（null = 查询所有用户，非空 = 只查该用户）
     * @return 会话分页结果，records 中每条包含会话信息 + 用户昵称 + 消息统计
     */
    @Select("<script>" +
            "SELECT s.id, s.user_id, s.session_title, s.started_at, s.last_emotion_analysis, s.last_emotion_updated_at, " +
            "       COALESCE(u.nickname, u.username) AS userNickname, " +
            "       (SELECT COUNT(*) FROM consultation_message m WHERE m.session_id = s.id) AS messageCount, " +
            "       (SELECT m.content FROM consultation_message m WHERE m.session_id = s.id ORDER BY m.created_at DESC, m.id DESC LIMIT 1) AS lastMessageContent, " +
            "       (SELECT m.created_at FROM consultation_message m WHERE m.session_id = s.id ORDER BY m.created_at DESC, m.id DESC LIMIT 1) AS lastMessageTime " +
            "FROM consultation_session s " +
            "LEFT JOIN user u ON s.user_id = u.id " +
            "<where>" +
            "  <if test='userId != null'> AND s.user_id = #{userId}</if>" +
            "</where>" +
            "ORDER BY s.started_at DESC" +
            "</script>")
    IPage<SessionPageResponseDTO> selectSessionPage(Page<SessionPageResponseDTO> page, @Param("userId") Long userId);
}
