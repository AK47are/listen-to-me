package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.CommentLikeDTO;
import com.github.listen_to_me.domain.entity.CommentLike;

public interface CommentLikeService extends IService<CommentLike> {
    void likeComment(CommentLikeDTO commentLikeDTO);
}
