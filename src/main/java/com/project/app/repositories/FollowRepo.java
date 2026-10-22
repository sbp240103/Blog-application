package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Follow;
import com.project.app.entities.User;

public interface FollowRepo extends JpaRepository<Follow, Integer> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // people who follow the given user
    Page<Follow> findByFollowing(User following, Pageable pageable);

    // people the given user follows
    Page<Follow> findByFollower(User follower, Pageable pageable);

    long countByFollowing(User following);

    long countByFollower(User follower);

    void deleteByFollowerOrFollowing(User follower, User following);

}
