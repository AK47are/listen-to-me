package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.CommentDTO;
import com.github.listen_to_me.domain.entity.Comment;

public interface CommentService extends IService<Comment> {
    void addComment(CommentDTO commentDTO);
}
