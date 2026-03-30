package com.youda.vo;

import lombok.Data;

import java.util.List;

/**
 * 章节VO
 */
@Data
public class ChapterVO {

    private Long chapterId;

    private String title;

    private List<VideoVO> videos;
}
