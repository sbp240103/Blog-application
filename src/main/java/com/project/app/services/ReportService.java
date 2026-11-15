package com.project.app.services;

import com.project.app.payloads.ReportDto;
import com.project.app.payloads.ReportRequest;

public interface ReportService {

    ReportDto createReport(ReportRequest request);

}
