package com.github.listen_to_me.domain.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TranscodeTaskDTO implements Serializable {
    private Long audioId;
    private String objectName;
    private Integer trialDuration;
}
