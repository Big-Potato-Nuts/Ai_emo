package org.example.AiSpringboot.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.AiSpringboot.DTO.Response.ConsultationMessageResponseDTO;
import org.example.AiSpringboot.Entity.ConsultationMessage;
import org.example.AiSpringboot.Entity.ConsultationSession;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Mapper.ConsultationMessageMapper;
import org.example.AiSpringboot.Mapper.ConsultationSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询消息服务
 * 职责：
 * 1. 保存用户消息（原有）
 * 2. 保存 AI 消息（原有）
 * 3. 统计会话消息数（原有）
 * 4. 查询会话最后一条消息（原有）
 * 5. 新增：按会话查询全部消息列表（会话详情用，时间正序）
 */
@Service
public class ConsultationMessageService {

    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    /**
     * 保存用户消息（原有方法）
     *
     * @param sessionId 会话ID
     * @param contend   消息内容
     * @param emotion_tag 情绪标签（可为空）
     * @return 保存后的消息实体
     */
    public ConsultationMessage saveUserMessage(Long sessionId, String contend, String emotion_tag) {
        // 1. 构建用户消息实体
        ConsultationMessage userMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(1)
                .messageType(1)
                .content(contend)
                .emotionTag(emotion_tag)
                .createdAt(LocalDateTime.now())
                .build();
        // 2. 写入数据库
        consultationMessageMapper.insert(userMessage);
        return userMessage;
    }

    /**
     * 保存 AI 消息（原有方法）
     *
     * @param sessionId 会话ID
     * @param contend   消息内容
     * @param aiModel   使用的 AI 模型名称
     * @return 保存后的消息实体
     */
    public ConsultationMessage saveAiMessage(Long sessionId, String contend, String aiModel) {
        // 1. 构建 AI 消息实体（发送者类型为 2）
        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(2)
                .messageType(1)
                .content(contend)
                .aiModel(aiModel)
                .createdAt(LocalDateTime.now())
                .build();
        // 2. 写入数据库
        consultationMessageMapper.insert(message);
        return message;
    }

    /**
     * 统计会话消息总数（原有方法）
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    public Integer getMessageCountBySessionId(Long sessionId) {
        // 1. 构建按会话ID过滤的查询条件
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId);
        // 2. 统计数量并转 int
        Long count = consultationMessageMapper.selectCount(queryWrapper);
        return count.intValue();
    }

    /**
     * 查询会话最后一条消息（原有方法）
     *
     * @param sessionId 会话ID
     * @return 最后一条消息的响应 DTO
     */
    public ConsultationMessageResponseDTO getLastMessageBySessionId(Long sessionId) {
        // 1. 按创建时间倒序取第一条
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .orderByDesc(ConsultationMessage::getCreatedAt)
                .last("limit 1");
        ConsultationMessage consultationMessage = consultationMessageMapper.selectOne(queryWrapper);
        // 2. 转响应 DTO（无消息时返回 null）
        return consultationMessage != null ? convertToResponseDTO(consultationMessage) : null;
    }

    /**
     * 查询会话的全部消息列表（会话详情用）
     * 说明：先校验会话是否存在，再按创建时间正序返回全部消息；
     *      管理员可查任意会话，普通用户只能查自己的（归属校验在 Controller 层完成）
     *
     * @param sessionId 会话ID
     * @return 消息列表（时间正序，最早的消息在最前）
     */
    public List<ConsultationMessageResponseDTO> listMessagesBySessionId(Long sessionId) {
        // 1. 校验会话是否存在（不存在时前端会收到明确的业务错误）
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        // 2. 按会话ID查询全部消息，时间正序排列
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .orderByAsc(ConsultationMessage::getCreatedAt)
                .orderByAsc(ConsultationMessage::getId);
        List<ConsultationMessage> messages = consultationMessageMapper.selectList(queryWrapper);
        // 3. 逐条转换为响应 DTO
        return messages.stream().map(this::convertToResponseDTO).toList();
    }

    /**
     * 实体转响应 DTO（原有逻辑，保留）
     * 说明：手动逐字段赋值，并补充描述字段和消息长度
     */
    private ConsultationMessageResponseDTO convertToResponseDTO(ConsultationMessage message) {
        if (message == null) {
            return null;
        }
        // 手动逐字段赋值，确保转换的准确性和可控性
        ConsultationMessageResponseDTO responseDTO = new ConsultationMessageResponseDTO();
        responseDTO.setId(message.getId());
        responseDTO.setSessionId(message.getSessionId());
        responseDTO.setSenderType(message.getSenderType());
        responseDTO.setMessageType(message.getMessageType());
        responseDTO.setContent(message.getContent());
        responseDTO.setEmotionTag(message.getEmotionTag());
        responseDTO.setAiModel(message.getAiModel());
        responseDTO.setCreatedAt(message.getCreatedAt());

        // 设置描述字段（通过实体方法获取）
        responseDTO.setSenderTypeDesc(message.getSenderTypeDesc());
        responseDTO.setMessageTypeDesc(message.getMessageTypeDesc());

        // 计算消息长度
        responseDTO.calculateContentLength();

        return responseDTO;
    }
}
