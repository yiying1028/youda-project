package com.youda.vo;

import lombok.Data;

@Data
public class PointsRankingVO {

    private Long userId;

    private String nickname;

    private String avatar;

    private Integer points;

    private Integer rank;

    private Boolean currentUser;
}
