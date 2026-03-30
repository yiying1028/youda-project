package com.youda.vo;

import lombok.Data;

/**
 * 视频播放VO
 */
@Data
public class VideoPlayVO {

    private Long videoId;

    private String title;

    private String videoUrl;

    private Integer duration;

    private Integer lastPosition;

    private Long prevVideoId;

    private Long nextVideoId;
}
