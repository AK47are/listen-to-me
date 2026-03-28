package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class FolderVO {
      private Long folderId;
      private String name;
      private String description;
      private Long audioCount;
      private Long createTime;
}
