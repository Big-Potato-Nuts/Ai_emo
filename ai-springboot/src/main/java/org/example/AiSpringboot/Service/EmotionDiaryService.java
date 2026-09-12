package org.example.AiSpringboot.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.AiSpringboot.DTO.Command.EmotionDiaryCreatDTO;
import org.example.AiSpringboot.DTO.Response.EmotionDiaryAdminResponseDTO;
import org.example.AiSpringboot.Entity.EmotionDiary;
import org.example.AiSpringboot.Entity.User;
import org.example.AiSpringboot.Exception.BusinessException;
import org.example.AiSpringboot.Mapper.EmotionDiaryMapper;
import org.example.AiSpringboot.Mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 情绪日记服务
 * 职责：
 * 1. 用户新增日记（校验同一天只能记一条）
 * 2. 管理端分页查询全部日记（支持按用户ID、评分区间筛选，联表补出用户名昵称）
 * 3. 管理端删除日记
 */
@Service
public class EmotionDiaryService {

    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 新增情绪日记
     * 说明：表上有 (user_id, diary_date) 唯一约束，先预检重复，给出友好提示而非数据库报错
     *
     * @param userId 当前登录用户ID
     * @param dto    日记内容（日期、评分、情绪、内容、生活指标）
     * @return 保存后的日记实体
     */
    public EmotionDiary addDiary(Long userId, EmotionDiaryCreatDTO dto) {
        // 1. 校验该用户当天是否已记录过（重复则直接提示）
        LambdaQueryWrapper<EmotionDiary> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(EmotionDiary::getUserId, userId)
                .eq(EmotionDiary::getDiaryDate, dto.getDiaryDate());
        if (emotionDiaryMapper.selectCount(existsWrapper) > 0) {
            throw new BusinessException("今日情绪日记已记录，请勿重复提交");
        }
        // 2. 构建日记实体（创建时间取当前时间）
        EmotionDiary diary = EmotionDiary.builder()
                .userId(userId)
                .diaryDate(dto.getDiaryDate())
                .moodScore(dto.getMoodScore())
                .dominantEmotion(dto.getDominantEmotion())
                .emotionTriggers(dto.getEmotionTriggers())
                .diaryContent(dto.getDiaryContent())
                .sleepQuality(dto.getSleepQuality())
                .stressLevel(dto.getStressLevel())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        // 3. 写入数据库
        emotionDiaryMapper.insert(diary);
        return diary;
    }

    /**
     * 管理端分页查询日记
     * 说明：先按条件分页查出日记记录，再一次性查出涉及的 User 信息补全用户名/昵称
     *
     * @param pageNum        页码
     * @param pageSize       每页条数
     * @param filterUserId   按用户ID筛选（可为空）
     * @param moodScoreRange 按评分区间筛选（"1-3"/"4-6"/"7-10"，可为空）
     * @return 分页结果（含用户名、昵称）
     */
    public IPage<EmotionDiaryAdminResponseDTO> pageAdmin(long pageNum, long pageSize,
                                                         Long filterUserId, String moodScoreRange) {
        // 1. 构造分页对象
        Page<EmotionDiary> page = new Page<>(pageNum, pageSize);
        // 2. 构建查询条件：按用户ID、评分区间过滤
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(filterUserId != null, EmotionDiary::getUserId, filterUserId)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId);
        // 3. 解析评分区间参数（如 "1-3" 表示 1 <= mood_score <= 3）
        if (moodScoreRange != null && moodScoreRange.contains("-")) {
            String[] parts = moodScoreRange.split("-");
            try {
                Integer min = Integer.valueOf(parts[0]);
                Integer max = Integer.valueOf(parts[1]);
                wrapper.between(EmotionDiary::getMoodScore, min, max);
            } catch (NumberFormatException ignored) {
                // 区间参数格式非法时忽略该筛选条件，不影响查询
            }
        }
        // 4. 分页查询日记
        IPage<EmotionDiary> diaryPage = emotionDiaryMapper.selectPage(page, wrapper);
        List<EmotionDiary> records = diaryPage.getRecords();

        // 5. 批量查询涉及的 User，构建 userId -> User 映射（避免逐条查询数据库）
        List<Long> userIds = records.stream().map(EmotionDiary::getUserId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        // 6. 转换为响应 DTO（补全用户名、昵称）
        List<EmotionDiaryAdminResponseDTO> responseRecords = records.stream().map(diary -> {
            EmotionDiaryAdminResponseDTO dto = new EmotionDiaryAdminResponseDTO();
            dto.setId(diary.getId());
            dto.setUserId(diary.getUserId());
            // 从映射中取用户信息（用户可能已删除，兜底为空）
            User user = userMap.get(diary.getUserId());
            dto.setUsername(user != null ? user.getUsername() : "");
            dto.setNickname(user != null && user.getNickname() != null ? user.getNickname() : "");
            dto.setDiaryDate(diary.getDiaryDate());
            dto.setMoodScore(diary.getMoodScore());
            dto.setDominantEmotion(diary.getDominantEmotion());
            dto.setEmotionTriggers(diary.getEmotionTriggers());
            dto.setDiaryContent(diary.getDiaryContent());
            dto.setSleepQuality(diary.getSleepQuality());
            dto.setStressLevel(diary.getStressLevel());
            dto.setAiEmotionAnalysis(diary.getAiEmotionAnalysis());
            dto.setCreatedAt(diary.getCreatedAt());
            dto.setUpdatedAt(diary.getUpdatedAt());
            return dto;
        }).toList();

        // 7. 组装新的分页对象返回（MyBatis-Plus 分页对象 records 需要替换为响应 DTO）
        Page<EmotionDiaryAdminResponseDTO> responsePage = new Page<>(pageNum, pageSize, diaryPage.getTotal());
        responsePage.setRecords(responseRecords);
        return responsePage;
    }

    /**
     * 管理端删除日记
     *
     * @param diaryId 日记ID
     */
    public void deleteDiary(Long diaryId) {
        // 1. 校验日记是否存在
        if (emotionDiaryMapper.selectById(diaryId) == null) {
            throw new BusinessException("日记不存在");
        }
        // 2. 执行删除
        emotionDiaryMapper.deleteById(diaryId);
    }
}
