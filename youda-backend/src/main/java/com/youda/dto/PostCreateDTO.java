package com.youda.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发布帖子DTO
 */
@Data
public class PostCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度5-100个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "请选择分类")
    private Long categoryId;
}
