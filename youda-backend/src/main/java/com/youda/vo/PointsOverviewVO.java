package com.youda.vo;

import lombok.Data;

@Data
public class PointsOverviewVO {

    private Integer points;

    private Integer todayEarnedPoints;

    private Integer continuousDays;

    private Integer totalCheckinDays;

    private Boolean todayCheckedIn;

    private Integer currentRank;
}
