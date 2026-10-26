package com.zzztie.careerhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzztie.careerhub.domain.BaseEntity;
import com.zzztie.careerhub.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
