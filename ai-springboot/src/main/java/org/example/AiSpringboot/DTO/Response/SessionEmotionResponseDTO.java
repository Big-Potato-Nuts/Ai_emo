package org.example.AiSpringboot.DTO.Response;

// 会话情绪分析结果响应 DTO

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 会话情绪分析结果
 * 前端"情绪花园"卡片展示用，字段与 consultation.vue 的 currentEmotion 完全对应：
 * primaryEmotion 主要情绪、emotionScore 情绪评分(0-100)、isNegative 是否负面、
 * riskLevel 风险等级(0正常/1关注/2预警/3危机)、suggestion 建议、improvementSuggestions 改善建议列表
 */
@Data
@Builder
public class SessionEmotionResponseDTO {

    // 主要情绪（如：中性、开心、焦虑、悲伤）
    private String primaryEmotion;

    // 情绪评分（0-100，50 为中性基准）
    private Integer emotionScore;

    // 是否为负面情绪（true=需要关注，false=状态良好）
    private Boolean isNegative;

    // 风险等级（0-正常 1-关注 2-预警 3-危机）
    private Integer riskLevel;

    // 给用户的建议（一句话总结）
    private String suggestion;

    // 风险描述（风险等级 > 0 时说明具体风险）
    private String riskDescription;

    // 改善建议列表（多条可执行的建议）
    private List<String> improvementSuggestions;
}
