package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.RefreshToken;
import com.project.app.entities.User;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

}
