package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.LoginDTO;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.LoginVO;

public interface ISysUserService extends IService<SysUser> {

    LoginVO loginUser(LoginDTO loginDTO);

    LoginVO refreshToken();

}
