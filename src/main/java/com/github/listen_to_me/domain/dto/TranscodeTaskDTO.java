package com.github.listen_to_me.domain.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class TranscodeTaskDTO implements Serializable {
    private Long audioId;
    private String objectName;
    private Integer trialDuration;
}