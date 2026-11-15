package com.project.app.payloads;

import com.project.app.entities.ReportTargetType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReportRequest {

    @NotNull(message = "targetType is required, POST or COMMENT")
    private ReportTargetType targetType;

    @NotNull(message = "targetId is required")
    private Integer targetId;

    @NotEmpty(message = "reason should not be empty")
    @Size(min = 5, max = 500, message = "reason must be between 5 and 500 characters")
    private String reason;

}
