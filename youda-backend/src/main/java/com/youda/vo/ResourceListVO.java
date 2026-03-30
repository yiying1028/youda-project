package com.youda.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资料列表VO
 */
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

    private LocalDateTime createTime;
}
