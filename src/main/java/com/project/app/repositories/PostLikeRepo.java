package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Post;
import com.project.app.entities.PostLike;
import com.project.app.entities.User;

public interface PostLikeRepo extends JpaRepository<PostLike, Integer> {

    boolean existsByUserAndPost(User user, Post post);

    Optional<PostLike> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    void deleteByUser(User user);

}
