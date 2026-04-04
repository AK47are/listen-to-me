package com.github.listen_to_me.domain.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreatorPageQuery extends PageQuery {
    private String keyword;
}
