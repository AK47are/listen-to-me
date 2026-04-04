package com.github.listen_to_me.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.common.exception.BaseException;
import com.github.listen_to_me.common.util.MinioUtils;
import com.github.listen_to_me.domain.entity.SysUser;
import com.github.listen_to_me.domain.entity.UserFollow;
import com.github.listen_to_me.domain.query.PageQuery;
import com.github.listen_to_me.domain.vo.CreatorVO;
import com.github.listen_to_me.domain.vo.FansVO;
import com.github.listen_to_me.mapper.UserFollowMapper;
import com.github.listen_to_me.service.ISysUserService;
import com.github.listen_to_me.service.IUserFollowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements IUserFollowService {

    private final ISysUserService iSysUserService;

    @Override
    @Transactional
    public void follow(Long userId, Long creatorId) {
        log.debug("关注创作者 - 用户ID: {}, 创作者ID: {}", userId, creatorId);

        if (userId.equals(creatorId)) {
            throw new BaseException(400, "不能关注自己");
        }

        // 校验被关注者是否为创作者
        SysUser user = iSysUserService.getById(creatorId);
        if (user == null || !user.getIsCreator()) {
            throw new BaseException(400, "该用户不是创作者");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getCreatorId, creatorId);
        if (count(wrapper) > 0) {
            throw new BaseException(409, "已关注该创作者");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setUserId(userId);
        userFollow.setCreatorId(creatorId);
        save(userFollow);

        log.debug("关注成功 - 用户ID: {}, 创作者ID: {}", userId, creatorId);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long creatorId) {
        log.debug("取消关注 - 用户ID: {}, 创作者ID: {}", userId, creatorId);

        // 校验被关注者是否为创作者
        SysUser user = iSysUserService.getById(creatorId);
        if (user == null || !user.getIsCreator()) {
            throw new BaseException(400, "该用户不是创作者");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getCreatorId, creatorId);
        boolean removed = remove(wrapper);
        if (!removed) {
            throw new BaseException(404, "未关注该创作者");
        }

        log.debug("取消关注成功 - 用户ID: {}, 创作者ID: {}", userId, creatorId);
    }

    @Override
    public IPage<CreatorVO> getFollowPage(Long userId, PageQuery pageQuery) {
        log.debug("分页查询关注的创作者 - 用户ID: {}", userId);
        Page<CreatorVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        IPage<CreatorVO> result = baseMapper.selectFollowPage(page, userId);

        result.getRecords().forEach(vo -> vo.setAvatar(MinioUtils.getPresignedUrl(vo.getAvatar())));

        return result;
    }

    @Override
    public IPage<FansVO> getFansPage(Long creatorId, PageQuery pageQuery) {
        log.debug("分页查询粉丝列表 - 创作者ID: {}", creatorId);

        // 校验是否为创作者
        SysUser user = iSysUserService.getById(creatorId);
        if (user == null || !user.getIsCreator()) {
            throw new BaseException(400, "该用户不是创作者");
        }

        Page<FansVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        IPage<FansVO> result = baseMapper.selectFansPage(page, creatorId);

        result.getRecords().forEach(vo -> vo.setAvatar(MinioUtils.getPresignedUrl(vo.getAvatar())));

        return result;
    }
}
