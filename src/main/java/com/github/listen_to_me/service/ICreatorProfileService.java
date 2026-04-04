package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.entity.CreatorProfile;
import com.github.listen_to_me.domain.query.CreatorPageQuery;
import com.github.listen_to_me.domain.vo.CreatorVO;

/**
 * 创作者资料服务接口
 */
public interface ICreatorProfileService extends IService<CreatorProfile> {

    /**
     * 分页查询创作者列表
     *
     * @param userId 当前登录用户ID
     * @param query  分页查询条件
     * @return 分页结果
     */
    IPage<CreatorVO> getCreatorPage(Long userId, CreatorPageQuery query);
}
