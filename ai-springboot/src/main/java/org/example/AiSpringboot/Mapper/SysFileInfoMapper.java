package org.example.AiSpringboot.Mapper;

// 系统文件信息 Mapper 接口，对应表 sys_file_info

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.AiSpringboot.Entity.SysFileInfo;

/**
 * 系统文件信息 Mapper
 * 继承 BaseMapper 后自动获得文件记录表的增删改查能力
 */
@Mapper
public interface SysFileInfoMapper extends BaseMapper<SysFileInfo> {
}
