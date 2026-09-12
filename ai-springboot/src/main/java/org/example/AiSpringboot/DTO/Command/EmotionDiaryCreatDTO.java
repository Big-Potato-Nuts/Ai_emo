package org.example.AiSpringboot.DTO.Command;

// 新增情绪日记请求 DTO

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 新增情绪日记请求体
 * 字段与 emotionDiary.vue 提交的表单一致（diaryDate / moodScore / dominantEmotion / emotionTriggers /
 * diaryContent / sleepQuality / stressLevel）
 */
@Data
public class EmotionDiaryCreatDTO {

    // 日记日期（格式 yyyy-MM-dd，默认当天）
    @NotNull(message = "日记日期不能为空")
    private LocalDate diaryDate;

    // 情绪评分（1-10 分，必填）
    @NotNull(message = "情绪评分不能为空")
    @Min(value = 1, message = "情绪评分必须在1-10之间")
    @Max(value = 10, message = "情绪评分必须在1-10之间")
    private Integer moodScore;

    // 主要情绪（如：开心、平静、焦虑、悲伤）
    @Size(max = 50, message = "主要情绪长度不能超过50个字符")
    private String dominantEmotion;

    // 情绪触发因素（今天什么事情影响了情绪）
    @Size(max = 1000, message = "情绪触发因素不能超过1000个字符")
    private String emotionTriggers;

    // 日记内容（今日感想）
    @Size(max = 2000, message = "日记内容不能超过2000个字符")
    private String diaryContent;

    // 睡眠质量（1-5 分，可选）
    @Min(value = 1, message = "睡眠质量必须在1-5之间")
    @Max(value = 5, message = "睡眠质量必须在1-5之间")
    private Integer sleepQuality;

    // 压力水平（1-5 分，可选）
    @Min(value = 1, message = "压力水平必须在1-5之间")
    @Max(value = 5, message = "压力水平必须在1-5之间")
    private Integer stressLevel;
}
