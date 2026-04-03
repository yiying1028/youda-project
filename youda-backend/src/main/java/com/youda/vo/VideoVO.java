package com.youda.vo;

import lombok.Data;

@Data
public class VideoVO {

    private Long videoId;

    private String title;

    private Integer duration;

    private Boolean isFinished;
}