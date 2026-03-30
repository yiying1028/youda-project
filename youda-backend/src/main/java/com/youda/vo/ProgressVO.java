package com.youda.vo;

import lombok.Data;

/**
 * 学习进度VO
 */
@Data
public class ProgressVO {

    private Integer completedCount;

    private Integer totalCount;

    private Integer percentage;
}
