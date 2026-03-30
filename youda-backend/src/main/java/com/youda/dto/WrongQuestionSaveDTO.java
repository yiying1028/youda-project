package com.youda.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class WrongQuestionSaveDTO {

    @NotNull(message = "请选择学科")
    private Long subjectId;

    @NotNull(message = "请选择年级")
    private Long gradeId;

    @NotBlank(message = "请输入题目内容")
    private String questionContent;

    private String questionImage;

    private String myAnswer;

    @NotBlank(message = "请输入正确答案")
    private String correctAnswer;

    private String errorReason;
}
