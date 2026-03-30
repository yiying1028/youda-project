package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子详情VO
 */
@Data
public class PostDetailVO {

    private Long postId;

    private String title;

    private String content;

    private UserSimpleVO author;

    private Long categoryId;

    private String categoryName;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    private Boolean isLiked;

    private Boolean isCollected;

    private LocalDateTime createTime;
}
