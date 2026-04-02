package com.github.listen_to_me.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long userId;
    private String avatar;
    private String nickname;
    private String content;
    private Long parentId;
    private Long likeCount;
    private Boolean likedByCurrentUser;
    private LocalDateTime createTime;
    private List<CommentVO> replyList = new ArrayList<>();
}
