package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceListVO {

    private Long resourceId;

    private String name;

    private String description;

    private String subjectName;

    private String gradeName;

    private String fileType;

    private Long fileSize;

    private Integer downloadCount;

    private Boolean requiresPoints;

    private Integer pointsCost;

    private Boolean purchased;

    private Boolean canDownload;

    private LocalDateTime createTime;
}