package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Follow;
import com.project.app.entities.User;

public interface FollowRepo extends JpaRepository<Follow, Integer> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerOrFollowing(User follower, User following);

}
