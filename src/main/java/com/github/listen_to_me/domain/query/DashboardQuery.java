package com.github.listen_to_me.domain.query;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DashboardQuery {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
