package com.project.app.payloads;

import java.time.LocalDateTime;

import com.project.app.entities.ReportStatus;
import com.project.app.entities.ReportTargetType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ReportDto {

    private Integer id;
    private ReportTargetType targetType;
    private Integer targetId;
    private String reason;
    private ReportStatus status;
    private Integer reporterId;
    private String reporterName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

}
