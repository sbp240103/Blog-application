package com.project.app.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.app.entities.Report;
import com.project.app.entities.ReportStatus;
import com.project.app.entities.ReportTargetType;
import com.project.app.entities.User;

public interface ReportRepo extends JpaRepository<Report, Integer> {

    boolean existsByReporterAndTargetTypeAndTargetIdAndStatus(User reporter, ReportTargetType targetType, Integer targetId, ReportStatus status);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    void deleteByReporter(User reporter);

    // keep the report history when the moderator account is removed
    @Modifying
    @Query("update Report r set r.resolvedBy = null where r.resolvedBy = :user")
    void clearResolver(@Param("user") User user);

}
