package com.youda.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 密码修改DTO
 */
@Data
public class PasswordUpdateDTO {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
