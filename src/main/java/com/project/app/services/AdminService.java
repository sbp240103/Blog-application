package com.project.app.services;

import com.project.app.payloads.StatsDto;
import com.project.app.payloads.UserDto;
import com.project.app.entities.ReportStatus;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.ReportDto;

public interface AdminService {

    UserDto promoteToAdmin(Integer userId);

    UserDto demoteFromAdmin(Integer userId);

    StatsDto getStats();

    void banUser(Integer userId);

    void unbanUser(Integer userId);

    void setPostFeatured(Integer postId, boolean featured);

    PageResponse<ReportDto> getReports(ReportStatus status, Integer pageNumber, Integer pageSize);

    ReportDto resolveReport(Integer reportId, boolean removeContent);

    ReportDto dismissReport(Integer reportId);

}
