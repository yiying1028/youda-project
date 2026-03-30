package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资料详情VO
 */
@Data
public class ResourceDetailVO {

    private Long resourceId;

    private String name;

    private String description;

    private Long subjectId;

    private String subjectName;

    private Long gradeId;

    private String gradeName;

    private String fileType;

    private Long fileSize;

    private Integer downloadCount;

    private LocalDateTime createTime;
}
