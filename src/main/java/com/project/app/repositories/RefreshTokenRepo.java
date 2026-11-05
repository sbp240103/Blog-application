package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.RefreshToken;
import com.project.app.entities.User;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    @Modifying
    @Transactional
    @Query("delete from RefreshToken r where r.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

}
