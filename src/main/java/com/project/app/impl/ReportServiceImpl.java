package com.project.app.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.entities.Report;
import com.project.app.entities.ReportStatus;
import com.project.app.entities.ReportTargetType;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.payloads.ReportDto;
import com.project.app.payloads.ReportRequest;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.repositories.ReportRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.ReportService;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepo reportRepo;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ReportDto createReport(ReportRequest request) {
        boolean exists = request.getTargetType() == ReportTargetType.POST
                ? this.postRepo.existsById(request.getTargetId())
                : this.commentRepo.existsById(request.getTargetId());
        if (!exists) {
            throw new ApiException(request.getTargetType().name().toLowerCase() + " not found : " + request.getTargetId(), HttpStatus.NOT_FOUND);
        }

        User reporter = this.authUtil.getLoggedInUser();
        if (this.reportRepo.existsByReporterAndTargetTypeAndTargetIdAndStatus(reporter, request.getTargetType(), request.getTargetId(), ReportStatus.OPEN)) {
            throw new ApiException("you already reported this, moderators will review it", HttpStatus.CONFLICT);
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason().trim());
        return toDto(this.reportRepo.save(report));
    }

    public static ReportDto toDto(Report report) {
        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setReason(report.getReason());
        dto.setStatus(report.getStatus());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterName(report.getReporter().getName());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        return dto;
    }

}
