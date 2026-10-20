package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Bookmark;
import com.project.app.entities.Post;
import com.project.app.entities.User;

public interface BookmarkRepo extends JpaRepository<Bookmark, Integer> {

    boolean existsByUserAndPost(User user, Post post);

    Optional<Bookmark> findByUserAndPost(User user, Post post);

    Page<Bookmark> findByUser(User user, Pageable pageable);

    void deleteByUser(User user);

}
