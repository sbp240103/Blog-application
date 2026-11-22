package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.payloads.ReportDto;
import com.project.app.payloads.ReportRequest;
import com.project.app.services.ReportService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Reports", description = "reporting posts and comments")
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // flag a post or comment for the moderators
    @PostMapping
    public ResponseEntity<ReportDto> createReport(@Valid @RequestBody ReportRequest request) {
        return new ResponseEntity<>(this.reportService.createReport(request), HttpStatus.CREATED);
    }

}
