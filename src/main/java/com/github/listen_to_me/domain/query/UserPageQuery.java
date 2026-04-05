package com.github.listen_to_me.domain.query;

import lombok.Data;

@Data
public class UserPageQuery extends PageQuery {
    String username;
}
