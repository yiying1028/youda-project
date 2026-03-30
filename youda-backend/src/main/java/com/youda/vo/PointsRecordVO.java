package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsRecordVO {

    private String actionType;

    private String actionLabel;

    private Integer points;

    private String remark;

    private LocalDateTime createTime;
}
