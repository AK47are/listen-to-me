package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.UserProfileUpdateDTO;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.UserVO;

public interface ISysUserService extends IService<SysUser> {

    UserVO findProfile();

    void modifyProfile(UserProfileUpdateDTO updateDTO);
}
