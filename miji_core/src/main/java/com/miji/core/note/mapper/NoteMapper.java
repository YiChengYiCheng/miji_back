package com.miji.core.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.DO.NoteDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<NoteDO> {
}
