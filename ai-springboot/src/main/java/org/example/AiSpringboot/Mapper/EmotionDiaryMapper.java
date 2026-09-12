package org.example.AiSpringboot.Mapper;

// 情绪日记 Mapper 接口，对应表 emotion_diary

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.AiSpringboot.Entity.EmotionDiary;

/**
 * 情绪日记 Mapper
 * 继承 BaseMapper 后自动获得：单表增删改查、条件查询（selectList/selectOne/selectCount）等能力
 */
@Mapper
public interface EmotionDiaryMapper extends BaseMapper<EmotionDiary> {
}
