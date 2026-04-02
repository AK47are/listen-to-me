package com.github.listen_to_me.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.listen_to_me.domain.dto.CommentDTO;
import com.github.listen_to_me.domain.entity.Comment;
import com.github.listen_to_me.domain.query.CommentQuery;
import com.github.listen_to_me.domain.vo.CommentVO;

public interface CommentService extends IService<Comment> {
    void addComment(CommentDTO commentDTO);

    IPage<CommentVO> findCommentPage(CommentQuery commentQuery);
}
