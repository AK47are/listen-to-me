package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.entity.UserFollow;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.CreatorVO;
import com.github.listen_to_me.domain.vo.FansVO;

public interface IUserFollowService extends IService<UserFollow> {

    /**
     * 关注创作者
     *
     * @param userId    当前用户ID
     * @param creatorId 创作者ID
     */
    void follow(Long userId, Long creatorId);

    /**
     * 取消关注
     *
     * @param userId    当前用户ID
     * @param creatorId 创作者ID
     */
    void unfollow(Long userId, Long creatorId);

    /**
     * 分页查询当前用户关注的创作者列表
     *
     * @param userId    当前用户ID
     * @param pageQuery 分页查询条件
     * @return 分页结果
     */
    IPage<CreatorVO> getFollowPage(Long userId, PageQuery pageQuery);

    /**
     * 分页查询指定创作者的粉丝列表
     *
     * @param creatorId 创作者ID
     * @param pageQuery 分页查询条件
     * @return 分页结果
     */
    IPage<FansVO> getFansPage(Long creatorId, PageQuery pageQuery);
}
