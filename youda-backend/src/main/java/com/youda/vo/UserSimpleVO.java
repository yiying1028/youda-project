package com.youda.vo;

import lombok.Data;

/**
 * 简单用户信息VO（用于列表展示）
 */
@Data
public class UserSimpleVO {

    private Long userId;

    private String nickname;

    private String avatar;
}
