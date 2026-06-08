package com.miji.core.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.DO.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
