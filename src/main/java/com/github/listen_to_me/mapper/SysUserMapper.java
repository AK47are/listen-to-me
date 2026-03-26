package com.github.listen_to_me.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.listen_to_me.domain.entity.SysUser;

public interface SysUserMapper extends BaseMapper<SysUser> {

    // 获取指定用户的权限情况
    List<String> selectPermCodeListById(Long userId);

}
