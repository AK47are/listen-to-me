package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;

    /**
     * 创作者标识：0-听众, 1-创作者
     * 前端根据此字段决定是否展示「创作中心」入口
     */
    private Integer isCreator;
}
