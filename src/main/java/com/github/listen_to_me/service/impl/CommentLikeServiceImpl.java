package com.github.listen_to_me.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.listen_to_me.domain.entity.CommentLike;
import com.github.listen_to_me.mapper.CommentLikeMapper;
import com.github.listen_to_me.service.CommentLikeService;
import org.springframework.stereotype.Service;

@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {
}
