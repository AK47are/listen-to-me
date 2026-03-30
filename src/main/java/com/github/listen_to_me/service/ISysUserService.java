package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.UserProfileUpdateDTO;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.vo.BalanceVO;
import com.github.listen_to_me.domain.vo.UserVO;

public interface ISysUserService extends IService<SysUser> {

    UserVO findProfile();

    void modifyProfile(UserProfileUpdateDTO updateDTO);

    /**
     * 获取用户余额统计信息
     * 
     * @param userId 用户ID
     * @return 余额统计VO
     */
    BalanceVO getBalanceStats(Long userId);
}
