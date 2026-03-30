package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WrongQuestionVO {

    private Long id;

    private Long subjectId;

    private String subjectName;

    private Long gradeId;

    private String gradeName;

    private String questionContent;

    private String questionImage;

    private String myAnswer;

    private String correctAnswer;

    private String errorReason;

    private Integer masteryStatus;

    private String masteryStatusText;

    private LocalDateTime createTime;
}
