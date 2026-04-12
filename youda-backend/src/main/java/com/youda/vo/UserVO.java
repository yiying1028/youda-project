package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private String bio;

    private Integer points;

    private Integer virtualBalance;

    private Integer role;

    private LocalDateTime createTime;
}