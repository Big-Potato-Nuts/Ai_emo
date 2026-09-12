package org.example.AiSpringboot.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.AiSpringboot.Entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
