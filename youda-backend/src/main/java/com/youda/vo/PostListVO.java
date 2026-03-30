package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子列表VO
 */
@Data
public class PostListVO {

    private Long postId;

    private String title;

    private String summary;

    private UserSimpleVO author;

    private String categoryName;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Boolean isTop;

    private LocalDateTime createTime;
}
