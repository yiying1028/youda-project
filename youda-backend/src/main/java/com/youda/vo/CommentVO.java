package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论VO
 */
@Data
public class CommentVO {

    private Long commentId;

    private String content;

    private UserSimpleVO author;

    private LocalDateTime createTime;
}
